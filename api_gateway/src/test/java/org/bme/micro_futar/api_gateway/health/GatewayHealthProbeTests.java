package org.bme.micro_futar.api_gateway.health;

import org.bme.micro_futar.api_gateway.ratelimit.AbstractGatewayRateLimitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** A Kubernetes probe-ok végpontjai. A Redis szándékosan elérhetetlen: a probe-ok nem függhetnek tőle. */
class GatewayHealthProbeTests extends AbstractGatewayRateLimitTest {

    @LocalManagementPort
    private int managementPort;

    private WebTestClient managementClient;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("REDIS_HOST", () -> "127.0.0.1");
        registry.add("REDIS_PORT", () -> 1);
    }

    @BeforeEach
    void createManagementClient() {
        managementClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + managementPort).build();
    }

    // Pontosan 200 kell: a 302-es bejelentkezési átirányítást a Kubernetes sikernek venné.
    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health/liveness", "/actuator/health/readiness"})
    void probesAnswerWithoutAuthenticationOnManagementPort(String path) {
        managementClient.get().uri(path).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.components").doesNotExist();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"})
    void healthIsNotServedOnMainPort(String path) {
        client.get().uri(path).exchange().expectStatus().isNotFound();
    }
}

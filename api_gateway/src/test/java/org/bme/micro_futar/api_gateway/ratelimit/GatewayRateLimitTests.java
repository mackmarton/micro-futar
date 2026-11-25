package org.bme.micro_futar.api_gateway.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class GatewayRateLimitTests extends AbstractGatewayRateLimitTest {

    private static final String NEW_SHIPMENT = "/api/orders/shipments/new";

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("REDIS_HOST", REDIS::getHost);
        registry.add("REDIS_PORT", () -> REDIS.getMappedPort(6379));
        // Szűk alapkorlát, hogy kiderüljön, ha a webhook az általános payment route-ra esne.
        registry.add("RATE_LIMIT_DEFAULT_REPLENISH_RATE", () -> 1);
        registry.add("RATE_LIMIT_DEFAULT_BURST_CAPACITY", () -> 2);
    }

    @Test
    void shipmentCreationIsLimitedAfterTheBurst() {
        String forwardedFor = "203.0.113.10";
        for (int i = 0; i < 10; i++) {
            call(HttpMethod.POST, NEW_SHIPMENT, forwardedFor).expectStatus().isOk();
        }

        call(HttpMethod.POST, NEW_SHIPMENT, forwardedFor)
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectHeader().valueEquals(HttpHeaders.RETRY_AFTER, "12")
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173");
    }

    @Test
    void spoofedForwardedForEntriesDoNotCreateNewBuckets() {
        for (int i = 0; i < 10; i++) {
            call(HttpMethod.POST, NEW_SHIPMENT, "198.51.100." + i + ", 203.0.113.20").expectStatus().isOk();
        }

        call(HttpMethod.POST, NEW_SHIPMENT, "198.51.100.99, 203.0.113.20")
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        // Másik kliens saját vödröt kap.
        call(HttpMethod.POST, NEW_SHIPMENT, "203.0.113.21").expectStatus().isOk();
    }

    @Test
    void stripeWebhookIsNeverLimited() {
        String forwardedFor = "203.0.113.30";
        for (int i = 0; i < 100; i++) {
            call(HttpMethod.POST, "/api/payments/webhooks/stripe", forwardedFor).expectStatus().isOk();
        }

        // Kontroll: ugyanennek a kliensnek az általános payment route-on a korlát érvényes.
        for (int i = 0; i < 2; i++) {
            call(HttpMethod.GET, "/api/payments/other", forwardedFor).expectStatus().isOk();
        }
        call(HttpMethod.GET, "/api/payments/other", forwardedFor)
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void checkoutAndTrackingHaveTheirOwnLimits() {
        String forwardedFor = "203.0.113.40";
        for (int i = 0; i < 10; i++) {
            call(HttpMethod.POST, "/api/payments/checkout", forwardedFor).expectStatus().isOk();
        }
        call(HttpMethod.POST, "/api/payments/checkout", forwardedFor)
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectHeader().valueEquals(HttpHeaders.RETRY_AFTER, "6");

        // A tracking vödre ettől független; 2/s feltöltés mellett a 20-as löket után hamar 429 jön.
        call(HttpMethod.GET, "/api/tracking/abc", forwardedFor)
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-RateLimit-Burst-Capacity", "20")
                .expectHeader().valueEquals("X-RateLimit-Replenish-Rate", "2");
    }

    @Test
    void passesWhileRedisIsPausedAndLimitsAgainAfterwards() {
        String forwardedFor = "203.0.113.60";
        call(HttpMethod.POST, NEW_SHIPMENT, forwardedFor).expectStatus().isOk();

        REDIS.getDockerClient().pauseContainerCmd(REDIS.getContainerId()).exec();
        try {
            for (int i = 0; i < 12; i++) {
                call(HttpMethod.POST, NEW_SHIPMENT, forwardedFor).expectStatus().isOk();
            }
        } finally {
            REDIS.getDockerClient().unpauseContainerCmd(REDIS.getContainerId()).exec();
        }

        // A szünet alatt sorba állt parancsok is elfogyaszthattak tokent, ezért legfeljebb 11 kérésen belül jön a 429.
        int status = 200;
        for (int i = 0; i < 11 && status == 200; i++) {
            status = call(HttpMethod.POST, NEW_SHIPMENT, forwardedFor)
                    .returnResult(Void.class).getStatus().value();
        }
        assertThat(status).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}

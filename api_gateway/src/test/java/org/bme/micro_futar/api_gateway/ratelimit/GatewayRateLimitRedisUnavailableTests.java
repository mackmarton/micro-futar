package org.bme.micro_futar.api_gateway.ratelimit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;

/** A "Redis" fogadja a kapcsolatot, de sosem válaszol: a kéréseknek rövid időn belül át kell menniük. */
class GatewayRateLimitRedisUnavailableTests extends AbstractGatewayRateLimitTest {

    private static final ServerSocket SILENT_REDIS = openSocket();

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("REDIS_HOST", () -> "127.0.0.1");
        registry.add("REDIS_PORT", SILENT_REDIS::getLocalPort);
    }

    @AfterAll
    static void close() throws IOException {
        SILENT_REDIS.close();
    }

    @Test
    void requestsPassWhenRedisDoesNotAnswer() {
        client = client.mutate().responseTimeout(Duration.ofSeconds(3)).build();

        for (int i = 0; i < 12; i++) {
            call(HttpMethod.POST, "/api/orders/shipments/new", "203.0.113.50").expectStatus().isOk();
        }
    }

    private static ServerSocket openSocket() {
        try {
            return new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

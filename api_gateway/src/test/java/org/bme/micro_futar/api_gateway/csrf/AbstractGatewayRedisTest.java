package org.bme.micro_futar.api_gateway.csrf;

import org.bme.micro_futar.api_gateway.ratelimit.AbstractGatewayRateLimitTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

/** A teljes gateway (lásd az ősosztályt) valódi Redisszel a munkamenetekhez. */
abstract class AbstractGatewayRedisTest extends AbstractGatewayRateLimitTest {

    protected static final String UI_ORIGIN = "http://localhost:5173";

    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    static {
        // A Spring kontextust a tesztosztályok megosztják, ezért a konténer a JVM végéig fut (a Ryuk takarítja el).
        REDIS.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("REDIS_HOST", REDIS::getHost);
        registry.add("REDIS_PORT", () -> REDIS.getMappedPort(6379));
    }
}

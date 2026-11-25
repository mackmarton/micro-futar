package org.bme.micro_futar.api_gateway.ratelimit;

import org.bme.micro_futar.api_gateway.config.ratelimit.ClientKeyResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;

class ClientKeyResolverTests {

    private static final String PROXY = "10.42.0.7";

    @Test
    void usesAddressAppendedByTrustedProxyNotTheSpoofedOne() {
        ServerWebExchange exchange = exchange("1.2.3.4, 203.0.113.9");

        assertThat(new ClientKeyResolver(1).resolve(exchange).block()).isEqualTo("ip:203.0.113.9");
    }

    @Test
    void countsFromTheRightWithTwoProxies() {
        ServerWebExchange exchange = exchange("1.2.3.4, 203.0.113.9, 10.0.0.1");

        assertThat(new ClientKeyResolver(2).resolve(exchange).block()).isEqualTo("ip:203.0.113.9");
    }

    @Test
    void ignoresHeaderWithoutProxy() {
        ServerWebExchange exchange = exchange("1.2.3.4");

        assertThat(new ClientKeyResolver(0).resolve(exchange).block()).isEqualTo("ip:" + PROXY);
    }

    @Test
    void fallsBackToRemoteAddressWhenHeaderIsMissing() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/x").remoteAddress(new InetSocketAddress(PROXY, 40000)));

        assertThat(new ClientKeyResolver(1).resolve(exchange).block()).isEqualTo("ip:" + PROXY);
    }

    @Test
    void usesUserIdWhenLoggedIn() {
        Principal user = new TestingAuthenticationToken("user-123", "n/a");
        ServerWebExchange exchange = exchange("203.0.113.9").mutate().principal(Mono.just(user)).build();

        assertThat(new ClientKeyResolver(1).resolve(exchange).block()).isEqualTo("user:user-123");
    }

    private static ServerWebExchange exchange(String forwardedFor) {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders/x")
                .remoteAddress(new InetSocketAddress(PROXY, 40000))
                .header("X-Forwarded-For", forwardedFor));
    }
}

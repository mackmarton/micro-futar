package org.bme.micro_futar.api_gateway.redirect;

import org.bme.micro_futar.api_gateway.config.RedirectProperties;
import org.bme.micro_futar.api_gateway.config.RedirectUriPolicy;
import org.bme.micro_futar.api_gateway.config.SessionRedirectAuthenticationSuccessHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.WebFilterExchange;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SessionRedirectSuccessHandlerTests {

    private static final String DEFAULT_URL = "https://micro-futar.hu/";

    private final SessionRedirectAuthenticationSuccessHandler handler = new SessionRedirectAuthenticationSuccessHandler(
            DEFAULT_URL,
            new RedirectUriPolicy(
                    new RedirectProperties(List.of("https"), List.of(), List.of("micro-futar.hu")), DEFAULT_URL));

    // A második sor: a lista szigorítása előtt a munkamenetbe került cím.
    @ParameterizedTest
    @CsvSource({
            "https://app.micro-futar.hu/x, https://app.micro-futar.hu/x",
            "http://localhost:5173/, https://micro-futar.hu/"
    })
    void storedUrlIsCheckedAgainBeforeRedirecting(String stored, String expected) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        exchange.getSession().block().getAttributes()
                .put(SessionRedirectAuthenticationSuccessHandler.REDIRECT_URL, stored);

        handler.onAuthenticationSuccess(new WebFilterExchange(exchange, e -> null), null).block();

        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create(expected));
    }

    @Test
    void withoutAStoredUrlTheDefaultIsUsed() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        handler.onAuthenticationSuccess(new WebFilterExchange(exchange, e -> null), null).block();

        assertThat(exchange.getResponse().getHeaders().getLocation()).isEqualTo(URI.create(DEFAULT_URL));
    }
}

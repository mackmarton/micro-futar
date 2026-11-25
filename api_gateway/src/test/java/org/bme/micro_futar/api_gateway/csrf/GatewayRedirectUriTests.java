package org.bme.micro_futar.api_gateway.csrf;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

/** A trükkös redirect_uri értékek a teljes gatewayen, mindkét útvonalon. */
class GatewayRedirectUriTests extends AbstractGatewayRedisTest {

    @ParameterizedTest
    @ValueSource(strings = {"https://micro-futar.hu@evil.com", "https://evil.com@micro-futar.hu", "//evil.com",
            "https://evil.com/?x=micro-futar.hu", "https://micro-futar.hu.evil.com", "https://evilmicro-futar.hu",
            "javascript:alert(1)", "javascript://micro-futar.hu/%0Aalert(1)", "data:text/html,x", "not a uri"})
    void trickyRedirectUriIsRejected(String redirectUri) {
        client.get().uri("/oauth2/authorization/keycloak?redirect_uri={uri}", redirectUri)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
        client.post().uri("/logout?redirect_uri={uri}", redirectUri)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:5173", "https://app.micro-futar.hu"})
    void allowedRedirectUriStartsTheLogin(String redirectUri) {
        client.get().uri("/oauth2/authorization/keycloak?redirect_uri={uri}", redirectUri)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FOUND);
    }
}

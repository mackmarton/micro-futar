package org.bme.micro_futar.api_gateway.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayLogoutTests extends AbstractGatewayRedisTest {

    private static final String SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    private static final String REDIRECT_URI = "http://localhost:5174/#/portal";

    @Autowired
    private ReactiveSessionRepository<? extends Session> sessions;

    private String sessionId;

    @BeforeEach
    void logIn() {
        sessionId = createLoggedInSession(sessions);
    }

    @Test
    void getDoesNotLogOut() {
        client.get().uri("/logout?redirect_uri={uri}", REDIRECT_URI)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectHeader().doesNotExist(HttpHeaders.LOCATION);

        assertThat(isLoggedIn()).isTrue();
    }

    @Test
    void postLogsOutAndRedirectsToTheRedirectUri() {
        postLogout(REDIRECT_URI, UI_ORIGIN)
                .expectStatus().isEqualTo(HttpStatus.FOUND)
                .expectHeader().valueEquals(HttpHeaders.LOCATION, REDIRECT_URI);

        assertThat(isLoggedIn()).isFalse();
    }

    @Test
    void postWithoutRedirectUriGoesToTheDefaultUrl() {
        client.post().uri("/logout")
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FOUND)
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "http://localhost:5173/");

        assertThat(isLoggedIn()).isFalse();
    }

    @Test
    void foreignRedirectUriIsRejected() {
        postLogout("https://evil.example/", UI_ORIGIN).expectStatus().isBadRequest();

        assertThat(isLoggedIn()).isTrue();
    }

    @Test
    void postFromAForeignOriginIsRejected() {
        postLogout(REDIRECT_URI, "https://evil.example").expectStatus().isForbidden();

        assertThat(isLoggedIn()).isTrue();
    }

    // A SameSite=Lax miatt az idegen oldalról érkező POST süti nélküli: a válasz nem írhatja felül a böngésző sütijét.
    @Test
    void postWithoutASessionCookieSetsNoCookie() {
        for (String origin : new String[]{UI_ORIGIN, "https://evil.example"}) {
            client.post().uri("/logout?redirect_uri={uri}", REDIRECT_URI)
                    .header(HttpHeaders.ORIGIN, origin)
                    .exchange()
                    .expectHeader().doesNotExist(HttpHeaders.SET_COOKIE);
        }
    }

    // A felület rejtett űrlapja: teljes oldalas navigáció, egyedi fejléc nélkül.
    private WebTestClient.ResponseSpec postLogout(String redirectUri, String origin) {
        return client.post().uri("/logout?redirect_uri={uri}", redirectUri)
                .cookie("SESSION", sessionId)
                .header(HttpHeaders.ORIGIN, origin)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange();
    }

    private boolean isLoggedIn() {
        Session session = sessions.findById(sessionId).block();
        return session != null && session.getAttribute(SECURITY_CONTEXT) != null;
    }

    private static <S extends Session> String createLoggedInSession(ReactiveSessionRepository<S> repository) {
        DefaultOAuth2User user = new DefaultOAuth2User(
                AuthorityUtils.createAuthorityList("ROLE_USER"), Map.of("sub", "test-user"), "sub");
        S session = repository.createSession().block();
        session.setAttribute(SECURITY_CONTEXT,
                new SecurityContextImpl(new OAuth2AuthenticationToken(user, List.of(), "keycloak")));
        repository.save(session).block();
        return session.getId();
    }
}

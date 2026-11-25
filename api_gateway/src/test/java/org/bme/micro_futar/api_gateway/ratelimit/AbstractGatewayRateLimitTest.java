package org.bme.micro_futar.api_gateway.ratelimit;

import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;

/**
 * A teljes gateway a valódi route-okkal; a backendeket egy 200-at adó helyi csonk helyettesíti. A Kubernetes
 * cloud platform bekapcsolja a Netty forwarded-feldolgozását, mint éles környezetben.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.main.cloud-platform=kubernetes",
        "SPRING_PROFILE=test",
        "SERVER_PORT=0",
        "COOKIE_DOMAIN=",
        "COOKIE_SECURE=false",
        "REDIS_PASSWORD=",
        "KEYCLOAK_URL=http://keycloak.invalid",
        "CLIENT_SECRET=test",
        "DEFAULT_REDIRECT_URL=http://localhost:5173/",
        "CORS_ALLOWED_ORIGINS=http://localhost:5173",
        "RATE_LIMIT_TRUSTED_PROXY_COUNT=1"
})
@Import(AbstractGatewayRateLimitTest.ClientRegistrationTestConfig.class)
public abstract class AbstractGatewayRateLimitTest {

    private static final HttpServer BACKEND = startBackend();

    @Autowired
    protected WebTestClient client;

    @DynamicPropertySource
    static void backendProperties(DynamicPropertyRegistry registry) {
        String uri = "http://127.0.0.1:" + BACKEND.getAddress().getPort();
        for (String name : new String[]{"ORDER", "TRACKING", "LOGISTICS", "COURIER", "PAYMENT"}) {
            registry.add(name + "_SERVICE_URI", () -> uri);
        }
    }

    protected WebTestClient.ResponseSpec call(HttpMethod method, String path, String forwardedFor) {
        return client.method(method).uri(path)
                .header("X-Forwarded-For", forwardedFor)
                .header("Origin", "http://localhost:5173")
                .header("X-Requested-With", "micro-futar")
                .exchange();
    }

    private static HttpServer startBackend() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @TestConfiguration
    static class ClientRegistrationTestConfig {

        // Rögzített végpontok, hogy induláskor ne kelljen Keycloak discovery.
        @Bean
        ReactiveClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryReactiveClientRegistrationRepository(ClientRegistration.withRegistrationId("keycloak")
                    .clientId("api-gateway-client")
                    .clientSecret("test")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .authorizationUri("http://keycloak.invalid/auth")
                    .tokenUri("http://keycloak.invalid/token")
                    .build());
        }
    }
}

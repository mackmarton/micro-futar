package org.bme.micro_futar.payments.support;

import org.bme.micro_futar.payments.config.auth.JwtAuthConverter;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Tokens with the claims Keycloak issues. The authorities come from the service's own {@link JwtAuthConverter},
 * so the role mapping is tested too.
 */
public class TestJwts {

    private final JwtAuthConverter jwtAuthConverter;

    TestJwts(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    /** A verified user with the given client roles, named as Keycloak issues them (see JwtAuthConverter). */
    public RequestPostProcessor user(String email, String... roles) {
        return token(email, true, roles);
    }

    public RequestPostProcessor unverifiedUser(String email, String... roles) {
        return token(email, false, roles);
    }

    private RequestPostProcessor token(String email, boolean emailVerified, String... roles) {
        return jwt()
                .jwt(token -> token
                        .claim("preferred_username", email)
                        .claim("email", email)
                        .claim("email_verified", emailVerified)
                        .claim("resource_access", Map.of("api-gateway-client", Map.of("roles", List.of(roles)))))
                .authorities(token -> jwtAuthConverter.convert(token).getAuthorities());
    }
}

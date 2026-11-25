package org.bme.micro_futar.courier.config.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtDecoderConfigTests {

    private static final String ISSUER = "https://auth.example.test/realms/micro-futar-realm";
    private static final String AUDIENCE = "api-gateway-client";

    private static RSAKey key;
    private static HttpServer jwksServer;
    private static JwtDecoder decoder;

    @BeforeAll
    static void startJwksServer() throws IOException, JOSEException {
        key = new RSAKeyGenerator(2048).keyID("test-key").generate();
        byte[] jwks = new JWKSet(key.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);

        jwksServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        jwksServer.createContext("/certs", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jwks.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(jwks);
            }
        });
        jwksServer.start();

        String jwkSetUri = "http://localhost:" + jwksServer.getAddress().getPort() + "/certs";
        decoder = new JwtDecoderConfig().jwtDecoder(jwkSetUri, ISSUER, AUDIENCE);
    }

    @AfterAll
    static void stopJwksServer() {
        jwksServer.stop(0);
    }

    @Test
    void acceptsAudienceAsString() throws JOSEException {
        String token = token(claims -> claims.claim("aud", AUDIENCE));

        assertThat(decoder.decode(token).getAudience()).containsExactly(AUDIENCE);
    }

    @Test
    void acceptsAudienceAsList() throws JOSEException {
        String token = token(claims -> claims.claim("aud", List.of("account", AUDIENCE)));

        assertThat(decoder.decode(token).getAudience()).containsExactly("account", AUDIENCE);
    }

    @Test
    void rejectsWrongIssuer() throws JOSEException {
        String token = token(claims -> claims.issuer("https://auth.example.test/realms/other"));

        assertRejected(token, "iss");
    }

    @Test
    void rejectsMissingIssuer() throws JOSEException {
        String token = token(claims -> claims.issuer(null));

        assertRejected(token, "iss");
    }

    @Test
    void rejectsMissingAudience() throws JOSEException {
        String token = token(claims -> claims.claim("aud", null));

        assertRejected(token, "audience");
    }

    @Test
    void rejectsOtherAudience() throws JOSEException {
        String token = token(claims -> claims.claim("aud", "account"));

        assertRejected(token, "audience");
    }

    @Test
    void rejectsExpiredToken() throws JOSEException {
        String token = token(claims -> claims.expirationTime(Date.from(Instant.now().minus(1, ChronoUnit.HOURS))));

        assertRejected(token, "expired");
    }

    private static void assertRejected(String token, String reason) {
        assertThatThrownBy(() -> decoder.decode(token))
                .isInstanceOf(JwtValidationException.class)
                .hasMessageContaining(reason);
    }

    /** A signed token that is valid unless the customizer breaks it. */
    private static String token(UnaryOperator<JWTClaimsSet.Builder> customizer) throws JOSEException {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject("user")
                .issuer(ISSUER)
                .claim("aud", AUDIENCE)
                .issueTime(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expirationTime(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)));
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
                customizer.apply(claims).build());
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }
}

package org.bme.micro_futar.shared.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerifiedEmailTests {

    private static final String EMAIL = "john@example.com";

    private static Map<String, Object> claims(Object email, Object verified) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("email_verified", verified);
        return claims;
    }

    @Test
    void acceptsBooleanTrueAndStringTrue() {
        assertEquals(EMAIL, VerifiedEmail.from(claims(EMAIL, true)).orElseThrow());
        assertEquals(EMAIL, VerifiedEmail.from(claims(EMAIL, "true")).orElseThrow());
    }

    @Test
    void rejectsEverythingElseAsVerifiedFlag() {
        for (Object verified : new Object[]{false, "false", "TRUE", "yes", 1, null}) {
            assertTrue(VerifiedEmail.from(claims(EMAIL, verified)).isEmpty(), "email_verified = " + verified);
        }
        assertTrue(VerifiedEmail.from(Map.of("email", EMAIL)).isEmpty());
    }

    @Test
    void rejectsMissingBlankOrNonStringEmail() {
        for (Object email : new Object[]{null, "", "   ", 42}) {
            assertTrue(VerifiedEmail.from(claims(email, true)).isEmpty(), "email = " + email);
        }
        assertTrue(VerifiedEmail.from(Map.of("email_verified", true)).isEmpty());
    }
}

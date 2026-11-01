package org.bme.micro_futar.shared.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Payment tokens let a sender without an account prove that a shipment is theirs when paying for it. Orders
 * generates the token and returns it only to the client; only {@link #hash(String)} of it is shared with payments.
 */
public final class PaymentTokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PaymentTokens() {
    }

    /** 256 bits of randomness, URL-safe base64 without padding. */
    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Hex-encoded SHA-256 of the token. */
    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Constant-time comparison so the response time does not leak how much of the hash matched. */
    public static boolean matches(String token, String expectedHash) {
        if (token == null || token.isBlank() || expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(token).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}

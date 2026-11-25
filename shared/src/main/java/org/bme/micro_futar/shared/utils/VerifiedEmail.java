package org.bme.micro_futar.shared.utils;

import java.util.Map;
import java.util.Optional;

/** The only way a token's email may be used to identify a user. */
public final class VerifiedEmail {

    private VerifiedEmail() {
    }

    /**
     * Returns the {@code email} claim only if {@code email_verified} is true, either as a boolean or as the string
     * {@code "true"}. Anything else, including a missing claim, means not verified.
     */
    public static Optional<String> from(Map<String, Object> claims) {
        Object verified = claims.get("email_verified");
        if (!Boolean.TRUE.equals(verified) && !"true".equals(verified)) {
            return Optional.empty();
        }
        return claims.get("email") instanceof String email && !email.isBlank()
                ? Optional.of(email)
                : Optional.empty();
    }
}

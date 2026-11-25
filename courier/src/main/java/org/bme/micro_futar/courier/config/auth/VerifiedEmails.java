package org.bme.micro_futar.courier.config.auth;

import org.bme.micro_futar.shared.exceptions.EmailNotVerifiedException;
import org.bme.micro_futar.shared.utils.VerifiedEmail;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public final class VerifiedEmails {

    private VerifiedEmails() {
    }

    public static String require(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return VerifiedEmail.from(jwt.getClaims()).orElseThrow(EmailNotVerifiedException::new);
        }
        throw new EmailNotVerifiedException();
    }
}

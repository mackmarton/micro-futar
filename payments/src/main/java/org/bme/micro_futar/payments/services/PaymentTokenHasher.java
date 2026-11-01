package org.bme.micro_futar.payments.services;

import org.bme.micro_futar.shared.utils.PaymentTokens;
import org.springframework.stereotype.Component;

@Component
public class PaymentTokenHasher {

    public String hash(String token) {
        return PaymentTokens.hash(token);
    }

    public boolean matches(String token, String expectedHash) {
        return PaymentTokens.matches(token, expectedHash);
    }
}

package org.bme.micro_futar.payments.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTokenHasherTests {

    private final PaymentTokenHasher hasher = new PaymentTokenHasher();

    @Test
    void matchesOnlyTheOriginalToken() {
        String hash = hasher.hash("secret-token");

        assertThat(hash).hasSize(64);
        assertThat(hasher.matches("secret-token", hash)).isTrue();
        assertThat(hasher.matches("other-token", hash)).isFalse();
    }

    @Test
    void rejectsMissingToken() {
        String hash = hasher.hash("secret-token");

        assertThat(hasher.matches(null, hash)).isFalse();
        assertThat(hasher.matches(" ", hash)).isFalse();
    }
}

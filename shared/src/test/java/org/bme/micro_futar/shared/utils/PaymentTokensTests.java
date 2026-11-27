package org.bme.micro_futar.shared.utils;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTokensTests {

    private static final String TOKEN = "Zx7pQ2mK9sLd4VnB1cRt8YwE5uHj3GfA0oIiNkMbXqU";
    private static final String TOKEN_HASH = "466dcc8d8efa55e0467a7c1f3af556bb5ebed58e66fd183176a1376ead05b9d3";

    @Test
    void generatedTokenIs256BitsOfUrlSafeBase64WithoutPadding() {
        String token = PaymentTokens.generate();

        assertThat(token).matches("[A-Za-z0-9_-]{43}");
        assertThat(Base64.getUrlDecoder().decode(token)).hasSize(32);
    }

    @Test
    void everyGeneratedTokenIsDifferent() {
        String first = PaymentTokens.generate();
        String second = PaymentTokens.generate();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void hashIsTheHexEncodedSha256OfTheToken() {
        String hash = PaymentTokens.hash(TOKEN);

        assertThat(hash).isEqualTo(TOKEN_HASH);
    }

    @Test
    void tokenMatchesItsOwnHash() {
        boolean matches = PaymentTokens.matches(TOKEN, TOKEN_HASH);

        assertThat(matches).isTrue();
    }

    @Test
    void anotherTokenDoesNotMatchTheHash() {
        boolean matches = PaymentTokens.matches(PaymentTokens.generate(), TOKEN_HASH);

        assertThat(matches).isFalse();
    }

    @Test
    void missingTokenOrHashNeverMatches() {
        assertThat(PaymentTokens.matches(null, TOKEN_HASH)).isFalse();
        assertThat(PaymentTokens.matches("   ", PaymentTokens.hash("   "))).isFalse();
        assertThat(PaymentTokens.matches(TOKEN, null)).isFalse();
    }
}

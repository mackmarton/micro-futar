package org.bme.micro_futar.payments.services;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class StripeAmountsTests {

    @Test
    void hufIsSentAsWholeForintsInMinorUnits() {
        assertThat(StripeAmounts.toMinorUnits(new BigDecimal("2490"), "HUF")).isEqualTo(249_000L);
        assertThat(StripeAmounts.toMinorUnits(new BigDecimal("2490.60"), "huf")).isEqualTo(249_100L);
    }

    @Test
    void twoDecimalCurrencyKeepsCents() {
        assertThat(StripeAmounts.toMinorUnits(new BigDecimal("12.345"), "EUR")).isEqualTo(1235L);
    }

    @Test
    void zeroDecimalCurrencyIsNotScaled() {
        assertThat(StripeAmounts.toMinorUnits(new BigDecimal("500"), "JPY")).isEqualTo(500L);
        assertThat(StripeAmounts.fromMinorUnits(500L, "JPY")).isEqualByComparingTo("500");
    }

    @Test
    void roundTripsHuf() {
        assertThat(StripeAmounts.fromMinorUnits(249_000L, "HUF")).isEqualByComparingTo("2490");
    }
}

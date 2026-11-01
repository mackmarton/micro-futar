package org.bme.micro_futar.payments.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Set;

/** Converts between our decimal amounts and Stripe's integer minor units. */
public final class StripeAmounts {

    private static final Set<String> ZERO_DECIMAL = Set.of(
            "BIF", "CLP", "DJF", "GNF", "JPY", "KMF", "KRW", "MGA", "PYG", "RWF", "UGX", "VND", "VUV", "XAF",
            "XOF", "XPF");
    /** Two-decimal in the API, but Stripe only accepts whole units (amount divisible by 100). */
    private static final Set<String> WHOLE_UNITS_ONLY = Set.of("HUF", "TWD");

    private StripeAmounts() {
    }

    public static long toMinorUnits(BigDecimal amount, String currencyCode) {
        String currency = currencyCode.toUpperCase(Locale.ROOT);
        if (ZERO_DECIMAL.contains(currency)) {
            return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        }
        BigDecimal scaled = WHOLE_UNITS_ONLY.contains(currency)
                ? amount.setScale(0, RoundingMode.HALF_UP)
                : amount.setScale(2, RoundingMode.HALF_UP);
        return scaled.movePointRight(2).longValueExact();
    }

    public static BigDecimal fromMinorUnits(long minorUnits, String currencyCode) {
        String currency = currencyCode.toUpperCase(Locale.ROOT);
        if (ZERO_DECIMAL.contains(currency)) {
            return BigDecimal.valueOf(minorUnits);
        }
        return BigDecimal.valueOf(minorUnits).movePointLeft(2);
    }
}

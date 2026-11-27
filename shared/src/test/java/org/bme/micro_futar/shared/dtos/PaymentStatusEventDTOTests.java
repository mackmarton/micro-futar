package org.bme.micro_futar.shared.dtos;

import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusEventDTOTests {

    @Test
    void dedupKeyIsTheSameForTheSameStatusOfTheSameShipment() {
        var first = PaymentStatusEventDTO.of(42L, PaymentStatus.PAID, new BigDecimal("2490"), "HUF");
        var redelivered = PaymentStatusEventDTO.of(42L, PaymentStatus.PAID, new BigDecimal("2490"), "HUF");

        assertThat(first.getDedupKey()).isEqualTo("PAID:shipment:42");
        assertThat(redelivered.getDedupKey()).isEqualTo(first.getDedupKey());
    }
}

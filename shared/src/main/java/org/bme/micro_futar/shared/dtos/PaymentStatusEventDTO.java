package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Published by payments whenever the payment of a shipment changes state. Each status is reached at most once per
 * shipment, so {@code dedupKey} is derived from the status and the shipment id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEventDTO {
    private String dedupKey;
    private Long shipmentId;
    private PaymentStatus status;
    /** Authorized amount for {@code AUTHORIZED}, captured amount for {@code PAID}, otherwise may be null. */
    private BigDecimal amount;
    private String currencyCode;
    private ZonedDateTime occurredAt;

    public static PaymentStatusEventDTO of(Long shipmentId, PaymentStatus status, BigDecimal amount,
                                           String currencyCode) {
        return PaymentStatusEventDTO.builder()
                .dedupKey(status + ":shipment:" + shipmentId)
                .shipmentId(shipmentId)
                .status(status)
                .amount(amount)
                .currencyCode(currencyCode)
                .occurredAt(ZonedDateTime.now())
                .build();
    }
}

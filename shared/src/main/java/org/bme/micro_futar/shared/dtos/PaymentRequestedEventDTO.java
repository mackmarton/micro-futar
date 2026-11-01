package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Published by orders when a shipment is created and has to be paid for. Payments creates its local payment
 * record from it; the sender then starts the Stripe checkout with the shipment id and the payment token.
 * <p>
 * Orders can be placed without an account, so ownership is proven by a random payment token that orders returns
 * only to the client that created the shipment. Only its SHA-256 hash travels on Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestedEventDTO {
    private Long shipmentId;
    private String parcelNumber;
    /** Amount authorized at checkout; the final price set by logistics never exceeds it. */
    private BigDecimal maxAmount;
    private String currencyCode;
    private String senderEmail;
    /** Hex-encoded SHA-256 of the payment token handed to the client. */
    private String paymentTokenHash;
    private ZonedDateTime requestedAt;
}

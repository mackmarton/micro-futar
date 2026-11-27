package org.bme.micro_futar.payments.support;

import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.bme.micro_futar.shared.utils.PaymentTokens;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/** Valid objects with realistic values; a test changes only what its statement is about. */
public final class TestFixtures {

    public static final long SHIPMENT_ID = 1042L;
    public static final String SENDER_EMAIL = "kovacs.eszter@postafiok.hu";
    public static final String PARCEL_NUMBER = "6f1c2a9e-4b7d-4e53-9a18-2d0c5b8e7f41";
    /** The token orders handed to the sender; payments only knows its hash. */
    public static final String PAYMENT_TOKEN = "q7Vd2mXk9TfLwR4sHcY1uBzN6aJpE0gQxK3nMvD8iWo";

    private TestFixtures() {
    }

    /** The request for a small package from Budapest to Vienna: at most 4990 HUF. */
    public static PaymentRequestedEventDTO.PaymentRequestedEventDTOBuilder paymentRequest() {
        return PaymentRequestedEventDTO.builder()
                .shipmentId(SHIPMENT_ID)
                .parcelNumber(PARCEL_NUMBER)
                .maxAmount(new BigDecimal("4990"))
                .currencyCode("HUF")
                .senderEmail(SENDER_EMAIL)
                .paymentTokenHash(PaymentTokens.hash(PAYMENT_TOKEN))
                .requestedAt(ZonedDateTime.now());
    }

    /** The final price logistics computed for {@link #paymentRequest()}. */
    public static ShipmentPricedEventDTO.ShipmentPricedEventDTOBuilder shipmentPriced() {
        return ShipmentPricedEventDTO.builder()
                .shipmentId(SHIPMENT_ID)
                .finalAmount(new BigDecimal("4290"))
                .currencyCode("HUF")
                .pricedAt(ZonedDateTime.now());
    }

    /** The stored form of {@link #paymentRequest()}, waiting for the checkout. */
    public static Payment pendingPayment() {
        Payment payment = new Payment();
        payment.setShipmentId(SHIPMENT_ID);
        payment.setParcelNumber(PARCEL_NUMBER);
        payment.setSenderEmail(SENDER_EMAIL);
        payment.setPaymentTokenHash(PaymentTokens.hash(PAYMENT_TOKEN));
        payment.setMaxAmount(new BigDecimal("4990"));
        payment.setCurrencyCode("HUF");
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    /** {@link #pendingPayment()} after the card was authorized for the maximum price. */
    public static Payment authorizedPayment() {
        Payment payment = pendingPayment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setPaymentIntentId("pi_3QkT7bLx2eZvKYlo1n8RfD4a");
        return payment;
    }
}

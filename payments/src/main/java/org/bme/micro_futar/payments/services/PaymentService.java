package org.bme.micro_futar.payments.services;

import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.payments.dtos.CheckoutResponseDTO;
import org.bme.micro_futar.payments.dtos.PaymentStatusResponseDTO;
import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.exceptions.PaymentNotFoundException;
import org.bme.micro_futar.payments.exceptions.PaymentNotReadyException;
import org.bme.micro_futar.payments.exceptions.PaymentStateException;
import org.bme.micro_futar.payments.repositories.PaymentRepository;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTokenHasher paymentTokenHasher;
    private final StripeGateway stripeGateway;
    private final ApplicationEventPublisher eventPublisher;

    /** Idempotent: a redelivered request for an already known shipment is ignored. */
    @Transactional
    public void registerPaymentRequest(PaymentRequestedEventDTO request) {
        if (paymentRepository.existsByShipmentId(request.getShipmentId())) {
            log.info("Payment for shipment {} already registered, skipping", request.getShipmentId());
            return;
        }
        Payment payment = new Payment();
        payment.setShipmentId(request.getShipmentId());
        payment.setParcelNumber(request.getParcelNumber());
        payment.setSenderEmail(request.getSenderEmail());
        payment.setPaymentTokenHash(request.getPaymentTokenHash());
        payment.setMaxAmount(request.getMaxAmount());
        payment.setCurrencyCode(request.getCurrencyCode());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
    }

    /**
     * Returns a Stripe Checkout URL for the shipment. An still-open session is reused, so refreshing the page or
     * clicking twice does not create a second authorization.
     */
    @Transactional
    public CheckoutResponseDTO startCheckout(Long shipmentId, String paymentToken, String userEmail, String locale) {
        Payment payment = findOwnedPayment(shipmentId, paymentToken, userEmail);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentStateException(shipmentId, payment.getStatus());
        }
        ZonedDateTime now = ZonedDateTime.now();
        if (payment.getCheckoutUrl() != null && payment.getCheckoutExpiresAt() != null
                && payment.getCheckoutExpiresAt().isAfter(now.plusMinutes(5))) {
            return new CheckoutResponseDTO(payment.getCheckoutUrl());
        }
        if (payment.getCheckoutSessionId() != null && payment.getCheckoutExpiresAt().isAfter(now)) {
            stripeGateway.expireCheckoutSession(payment.getCheckoutSessionId());
        }
        ZonedDateTime expiresAt = now.plus(StripeGateway.CHECKOUT_TTL);
        Session session = stripeGateway.createCheckoutSession(payment, expiresAt, locale);
        payment.setCheckoutSessionId(session.getId());
        payment.setCheckoutUrl(session.getUrl());
        payment.setCheckoutExpiresAt(expiresAt);
        return new CheckoutResponseDTO(session.getUrl());
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponseDTO getStatus(Long shipmentId, String paymentToken, String userEmail) {
        Payment payment = findOwnedPayment(shipmentId, paymentToken, userEmail);
        return PaymentStatusResponseDTO.builder()
                .shipmentId(payment.getShipmentId())
                .status(payment.getStatus())
                .maxAmount(payment.getMaxAmount())
                .capturedAmount(payment.getCapturedAmount())
                .currencyCode(payment.getCurrencyCode())
                .build();
    }

    /** {@code payment_intent.amount_capturable_updated}: the card is authorized for the maximum price. */
    @Transactional
    public void markAuthorized(Long shipmentId, String paymentIntentId, long amountCapturableMinor) {
        Payment payment = getPayment(shipmentId);
        if (paymentIntentId.equals(payment.getPaymentIntentId())) {
            return;
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            // A second authorization (e.g. from a session that was replaced): release it so the card isn't held.
            log.warn("Releasing extra authorization {} for shipment {} in status {}",
                    paymentIntentId, shipmentId, payment.getStatus());
            stripeGateway.cancelPaymentIntent(paymentIntentId);
            return;
        }
        payment.setPaymentIntentId(paymentIntentId);
        changeStatus(payment, PaymentStatus.AUTHORIZED,
                StripeAmounts.fromMinorUnits(amountCapturableMinor, payment.getCurrencyCode()));
    }

    /** {@code payment_intent.succeeded}: normally already handled synchronously by {@link #capture}. */
    @Transactional
    public void markPaid(Long shipmentId, long amountReceivedMinor) {
        Payment payment = getPayment(shipmentId);
        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }
        BigDecimal amount = StripeAmounts.fromMinorUnits(amountReceivedMinor, payment.getCurrencyCode());
        payment.setCapturedAmount(amount);
        changeStatus(payment, PaymentStatus.PAID, amount);
    }

    /** {@code checkout.session.expired}: the sender never completed the checkout. */
    @Transactional
    public void markExpired(Long shipmentId, String checkoutSessionId) {
        Payment payment = getPayment(shipmentId);
        if (payment.getStatus() != PaymentStatus.PENDING
                || !checkoutSessionId.equals(payment.getCheckoutSessionId())
                // We expire a session ourselves when replacing it; that webhook can arrive before the new session
                // id is committed, so only a session that reached its own expiry time ends the payment.
                || payment.getCheckoutExpiresAt().isAfter(ZonedDateTime.now().plusMinutes(1))) {
            return;
        }
        changeStatus(payment, PaymentStatus.EXPIRED, null);
    }

    /** {@code payment_intent.canceled}: the authorization was released (manually or by Stripe after ~7 days). */
    @Transactional
    public void markCanceled(Long shipmentId, String paymentIntentId) {
        Payment payment = getPayment(shipmentId);
        if (!paymentIntentId.equals(payment.getPaymentIntentId())) {
            // An extra authorization we released ourselves; the real one is unaffected.
            return;
        }
        if (payment.getStatus() != PaymentStatus.AUTHORIZED && payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }
        changeStatus(payment, PaymentStatus.CANCELED, null);
    }

    /** Logistics priced the shipment: capture the final price, never more than what was authorized. */
    @Transactional
    public void capture(ShipmentPricedEventDTO priced) {
        Payment payment = getPayment(priced.getShipmentId());
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Shipment {} already paid, skipping capture", priced.getShipmentId());
            return;
        }
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new PaymentStateException(priced.getShipmentId(), payment.getStatus());
        }
        if (!payment.getCurrencyCode().equalsIgnoreCase(priced.getCurrencyCode())) {
            throw new IllegalArgumentException("Currency mismatch for shipment " + priced.getShipmentId() + ": "
                    + payment.getCurrencyCode() + " vs " + priced.getCurrencyCode());
        }
        BigDecimal amount = priced.getFinalAmount();
        if (amount.compareTo(payment.getMaxAmount()) > 0) {
            log.warn("Final price {} exceeds authorized {} for shipment {}, capturing the authorized amount",
                    amount, payment.getMaxAmount(), priced.getShipmentId());
            amount = payment.getMaxAmount();
        }
        PaymentIntent intent = stripeGateway.capture(payment, amount);
        BigDecimal captured = StripeAmounts.fromMinorUnits(intent.getAmountReceived(), payment.getCurrencyCode());
        payment.setCapturedAmount(captured);
        changeStatus(payment, PaymentStatus.PAID, captured);
    }

    private void changeStatus(Payment payment, PaymentStatus status, BigDecimal amount) {
        log.info("Payment for shipment {}: {} -> {}", payment.getShipmentId(), payment.getStatus(), status);
        payment.setStatus(status);
        paymentRepository.save(payment);
        // Sent to Kafka by PaymentStatusProducer only after this transaction commits.
        eventPublisher.publishEvent(
                PaymentStatusEventDTO.of(payment.getShipmentId(), status, amount, payment.getCurrencyCode()));
    }

    /**
     * The sender proves ownership with the payment token from order creation, or, when logged in, with the email
     * in their JWT (so they can also pay from another device, where the token is not stored).
     */
    private Payment findOwnedPayment(Long shipmentId, String paymentToken, String userEmail) {
        Payment payment = paymentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new PaymentNotReadyException(shipmentId));
        boolean ownerByEmail = userEmail != null && userEmail.equalsIgnoreCase(payment.getSenderEmail());
        if (!ownerByEmail && !paymentTokenHasher.matches(paymentToken, payment.getPaymentTokenHash())) {
            throw new PaymentNotFoundException();
        }
        return payment;
    }

    private Payment getPayment(Long shipmentId) {
        return paymentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new IllegalStateException("No payment for shipment " + shipmentId));
    }
}

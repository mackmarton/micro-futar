package org.bme.micro_futar.payments.services;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.payments.entities.ProcessedStripeEvent;
import org.bme.micro_futar.payments.repositories.ProcessedStripeEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final PaymentService paymentService;
    private final ProcessedStripeEventRepository processedStripeEventRepository;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    /** @throws SignatureVerificationException if the request did not come from Stripe */
    public Event verify(String payload, String signatureHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    }

    /** Handles the event and records it in the same transaction, so a redelivery after success is a no-op. */
    @Transactional
    public void handle(Event event) throws EventDataObjectDeserializationException {
        if (processedStripeEventRepository.existsById(event.getId())) {
            log.info("Stripe event {} already processed", event.getId());
            return;
        }
        switch (event.getType()) {
            case "payment_intent.amount_capturable_updated" -> {
                PaymentIntent intent = (PaymentIntent) dataObject(event);
                Long shipmentId = shipmentId(intent.getMetadata());
                if (shipmentId != null) {
                    paymentService.markAuthorized(shipmentId, intent.getId(), intent.getAmountCapturable());
                }
            }
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) dataObject(event);
                Long shipmentId = shipmentId(intent.getMetadata());
                if (shipmentId != null) {
                    paymentService.markPaid(shipmentId, intent.getAmountReceived());
                }
            }
            case "payment_intent.canceled" -> {
                PaymentIntent intent = (PaymentIntent) dataObject(event);
                Long shipmentId = shipmentId(intent.getMetadata());
                if (shipmentId != null) {
                    paymentService.markCanceled(shipmentId, intent.getId());
                }
            }
            case "checkout.session.expired" -> {
                Session session = (Session) dataObject(event);
                Long shipmentId = shipmentId(session.getMetadata());
                if (shipmentId != null) {
                    paymentService.markExpired(shipmentId, session.getId());
                }
            }
            default -> log.debug("Ignoring Stripe event type {}", event.getType());
        }
        processedStripeEventRepository.save(
                new ProcessedStripeEvent(event.getId(), event.getType(), ZonedDateTime.now()));
    }

    private static StripeObject dataObject(Event event) throws EventDataObjectDeserializationException {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        // Empty when the event's API version differs from the library's; the payload fields we use are stable.
        return deserializer.getObject().isPresent() ? deserializer.getObject().get() : deserializer.deserializeUnsafe();
    }

    private static Long shipmentId(Map<String, String> metadata) {
        String value = metadata == null ? null : metadata.get(StripeGateway.SHIPMENT_ID_METADATA);
        if (value == null) {
            // Not created by us (e.g. a test event from the dashboard).
            log.warn("Stripe object without shipmentId metadata, ignoring");
            return null;
        }
        return Long.valueOf(value);
    }
}

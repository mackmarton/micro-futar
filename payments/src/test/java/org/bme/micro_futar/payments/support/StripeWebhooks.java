package org.bme.micro_futar.payments.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.net.Webhook;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UncheckedIOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Webhook calls as Stripe makes them: an event as JSON, signed with the webhook secret of the test profile. */
public class StripeWebhooks {

    private final String secret;
    private final ObjectMapper objectMapper;

    StripeWebhooks(Environment environment, ObjectMapper objectMapper) {
        this.secret = environment.getRequiredProperty("stripe.webhook-secret");
        this.objectMapper = objectMapper;
    }

    /** An event about a payment intent of the shipment; {@code fields} are added to the intent. */
    public String paymentIntentEvent(String eventId, String type, long shipmentId, String paymentIntentId,
                                     Map<String, Object> fields) {
        Map<String, Object> intent = new HashMap<>(fields);
        intent.put("id", paymentIntentId);
        intent.put("object", "payment_intent");
        intent.put("metadata", Map.of("shipmentId", String.valueOf(shipmentId)));
        return event(eventId, type, Stripe.API_VERSION, intent);
    }

    public String checkoutSessionEvent(String eventId, String type, long shipmentId, String sessionId) {
        return event(eventId, type, Stripe.API_VERSION, Map.of(
                "id", sessionId,
                "object", "checkout.session",
                "metadata", Map.of("shipmentId", String.valueOf(shipmentId))));
    }

    public String event(String eventId, String type, String apiVersion, Map<String, Object> dataObject) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "id", eventId,
                    "object", "event",
                    "api_version", apiVersion,
                    "type", type,
                    "data", Map.of("object", dataObject)));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String signature(String payload) {
        return signature(payload, secret, Instant.now());
    }

    public String signature(String payload, String signingSecret, Instant signedAt) {
        try {
            return Webhook.Signature.generateSignatureHeader(payload, signingSecret, signedAt.getEpochSecond());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    public MockHttpServletRequestBuilder delivery(String payload) {
        return delivery(payload, signature(payload));
    }

    public MockHttpServletRequestBuilder delivery(String payload, String signatureHeader) {
        return post("/api/webhooks/stripe")
                .header("Stripe-Signature", signatureHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);
    }
}

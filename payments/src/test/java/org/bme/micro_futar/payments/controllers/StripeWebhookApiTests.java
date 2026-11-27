package org.bme.micro_futar.payments.controllers;

import com.stripe.Stripe;
import org.bme.micro_futar.payments.IntegrationTest;
import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.repositories.PaymentRepository;
import org.bme.micro_futar.payments.repositories.ProcessedStripeEventRepository;
import org.bme.micro_futar.payments.services.StripeGateway;
import org.bme.micro_futar.payments.support.KafkaTestClient;
import org.bme.micro_futar.payments.support.StripeWebhooks;
import org.bme.micro_futar.payments.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class StripeWebhookApiTests {

    private static final String EVENT_ID = "evt_3QkT7bLx2eZvKYlo1mWq5xTz";
    private static final String INTENT_ID = "pi_3QkT7bLx2eZvKYlo1n8RfD4a";
    private static final String SESSION_ID = "cs_test_a1Qm7";

    @Autowired private MockMvc mockMvc;
    @Autowired private StripeWebhooks stripeWebhooks;
    @Autowired private KafkaTestClient kafka;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private PaymentRepository payments;
    @Autowired private ProcessedStripeEventRepository processedEvents;
    @Autowired private StripeGateway stripeGateway;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TransactionTemplate transactionTemplate;

    @Test
    void authorizationIsStoredAndAnnounced() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        deliver(authorization(INTENT_ID));

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(stored().getPaymentIntentId()).isEqualTo(INTENT_ID);
        assertThat(announced()).singleElement().satisfies(event -> {
            assertThat(event.getShipmentId()).isEqualTo(TestFixtures.SHIPMENT_ID);
            assertThat(event.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
            assertThat(event.getAmount()).isEqualByComparingTo("4990");
            assertThat(event.getCurrencyCode()).isEqualTo("HUF");
        });
    }

    @Test
    void invalidSignatureIsRejectedAndChangesNothing() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        String payload = authorization(INTENT_ID);
        String forged = stripeWebhooks.signature(payload, "whsec_someone_elses_secret", Instant.now());

        mockMvc.perform(stripeWebhooks.delivery(payload, forged)).andExpect(status().isBadRequest());

        assertUntouched();
    }

    @Test
    void tamperedPayloadIsRejected() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        String payload = authorization(INTENT_ID);
        String signature = stripeWebhooks.signature(payload);

        mockMvc.perform(stripeWebhooks.delivery(payload.replace("499000", "100"), signature))
                .andExpect(status().isBadRequest());

        assertUntouched();
    }

    @Test
    void staleSignatureIsRejected() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        String payload = authorization(INTENT_ID);
        String old = stripeWebhooks.signature(payload, "whsec_dummy", Instant.now().minus(Duration.ofMinutes(10)));

        mockMvc.perform(stripeWebhooks.delivery(payload, old)).andExpect(status().isBadRequest());

        assertUntouched();
    }

    @Test
    void redeliveredEventIsProcessedOnce() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        String payload = authorization(INTENT_ID);

        deliver(payload);
        deliver(payload);

        assertThat(processedEvents.findAll()).hasSize(1);
        assertThat(announced()).hasSize(1);
    }

    /** Releasing the extra authorization leaves no trace in the payment; only the stored event id stops a repeat. */
    @Test
    void redeliveredExtraAuthorizationIsReleasedOnlyOnce() throws Exception {
        payments.save(TestFixtures.authorizedPayment());
        String payload = authorization("pi_3QkTB2Lx2eZvKYlo0hYt6GsN");

        deliver(payload);
        deliver(payload);

        verify(stripeGateway, times(1)).cancelPaymentIntent("pi_3QkTB2Lx2eZvKYlo0hYt6GsN");
        assertThat(stored().getPaymentIntentId()).isEqualTo(INTENT_ID);
        assertThat(announced()).isEmpty();
    }

    @Test
    void successEventMarksThePaymentPaid() throws Exception {
        payments.save(TestFixtures.authorizedPayment());

        deliver(stripeWebhooks.paymentIntentEvent(EVENT_ID, "payment_intent.succeeded", TestFixtures.SHIPMENT_ID,
                INTENT_ID, Map.of("amount_received", 429_000)));

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(stored().getCapturedAmount()).isEqualByComparingTo("4290");
        assertThat(announced()).singleElement()
                .satisfies(event -> assertThat(event.getAmount()).isEqualByComparingTo("4290"));
    }

    @Test
    void canceledAuthorizationCancelsThePayment() throws Exception {
        payments.save(TestFixtures.authorizedPayment());

        deliver(stripeWebhooks.paymentIntentEvent(EVENT_ID, "payment_intent.canceled", TestFixtures.SHIPMENT_ID,
                INTENT_ID, Map.of()));

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(announced()).singleElement()
                .extracting(PaymentStatusEventDTO::getStatus).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    void expiredCheckoutExpiresThePayment() throws Exception {
        Payment payment = TestFixtures.pendingPayment();
        payment.setCheckoutSessionId(SESSION_ID);
        payment.setCheckoutExpiresAt(ZonedDateTime.now());
        payments.save(payment);

        deliver(stripeWebhooks.checkoutSessionEvent(EVENT_ID, "checkout.session.expired", TestFixtures.SHIPMENT_ID,
                SESSION_ID));

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(announced()).singleElement()
                .extracting(PaymentStatusEventDTO::getStatus).isEqualTo(PaymentStatus.EXPIRED);
    }

    @Test
    void eventWithoutShipmentMetadataIsIgnored() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        deliver(stripeWebhooks.event(EVENT_ID, "payment_intent.amount_capturable_updated", Stripe.API_VERSION,
                Map.of("id", INTENT_ID, "object", "payment_intent", "amount_capturable", 499_000)));

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(announced()).isEmpty();
    }

    @Test
    void eventFromAnotherApiVersionIsProcessed() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        deliver(stripeWebhooks.event(EVENT_ID, "payment_intent.amount_capturable_updated", "2024-06-20",
                Map.of("id", INTENT_ID, "object", "payment_intent", "amount_capturable", 499_000,
                        "metadata", Map.of("shipmentId", String.valueOf(TestFixtures.SHIPMENT_ID)))));

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    @Test
    void eventForAnUnknownPaymentFailsAndIsProcessedOnRetry() throws Exception {
        String payload = authorization(INTENT_ID);

        mockMvc.perform(stripeWebhooks.delivery(payload)).andExpect(status().isInternalServerError());
        assertThat(processedEvents.findAll()).isEmpty();

        payments.save(TestFixtures.pendingPayment());
        deliver(payload);

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    /**
     * The test holds the payment's row while the webhook is working on it, then commits a newer version. The webhook
     * hits the optimistic lock, and Stripe's retry applies the event to the newer state.
     */
    @Test
    void concurrentChangeFailsTheWebhookAndTheRetrySucceeds() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        String payload = authorization(INTENT_ID);

        CompletableFuture<Integer> overtaken = transactionTemplate.execute(transaction -> {
            jdbcTemplate.update("UPDATE payment SET version = version + 1 WHERE shipment_id = ?",
                    TestFixtures.SHIPMENT_ID);
            CompletableFuture<Integer> response = CompletableFuture.supplyAsync(() -> responseStatus(payload));
            await().until(this::someoneWaitsForALock);
            return response;
        });

        assertThat(overtaken.get(10, TimeUnit.SECONDS)).isEqualTo(500);
        assertUntouched();

        deliver(payload);

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(announced()).hasSize(1);
    }

    private boolean someoneWaitsForALock() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM pg_locks WHERE NOT granted", Long.class) > 0;
    }

    private int responseStatus(String payload) {
        try {
            return mockMvc.perform(stripeWebhooks.delivery(payload)).andReturn().getResponse().getStatus();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String authorization(String paymentIntentId) {
        return stripeWebhooks.paymentIntentEvent(EVENT_ID, "payment_intent.amount_capturable_updated",
                TestFixtures.SHIPMENT_ID, paymentIntentId, Map.of("amount_capturable", 499_000));
    }

    private void deliver(String payload) throws Exception {
        mockMvc.perform(stripeWebhooks.delivery(payload)).andExpect(status().isOk());
    }

    private void assertUntouched() {
        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(stored().getPaymentIntentId()).isNull();
        assertThat(processedEvents.findAll()).isEmpty();
        assertThat(announced()).isEmpty();
        verifyNoInteractions(stripeGateway);
    }

    private Payment stored() {
        return payments.findByShipmentId(TestFixtures.SHIPMENT_ID).orElseThrow();
    }

    /** The status is sent after the commit without waiting for the broker, so flush before reading. */
    private List<PaymentStatusEventDTO> announced() {
        kafkaTemplate.flush();
        return kafka.received("payment-status-topic", PaymentStatusEventDTO.class);
    }
}

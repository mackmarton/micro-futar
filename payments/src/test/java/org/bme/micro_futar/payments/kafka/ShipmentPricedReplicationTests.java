package org.bme.micro_futar.payments.kafka;

import com.stripe.model.PaymentIntent;
import org.bme.micro_futar.payments.IntegrationTest;
import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.exceptions.StripeOperationException;
import org.bme.micro_futar.payments.repositories.PaymentRepository;
import org.bme.micro_futar.payments.services.StripeGateway;
import org.bme.micro_futar.payments.support.KafkaTestClient;
import org.bme.micro_futar.payments.support.StripeWebhooks;
import org.bme.micro_futar.payments.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ShipmentPricedReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private PaymentRepository payments;
    @Autowired private StripeGateway stripeGateway;
    @Autowired private StripeWebhooks stripeWebhooks;
    @Autowired private MockMvc mockMvc;

    @Test
    void finalPriceIsCapturedAndAnnouncedAsPaid() {
        payments.save(TestFixtures.authorizedPayment());
        when(stripeGateway.capture(any(), any())).thenReturn(received(429_000));

        publish(TestFixtures.shipmentPriced().build());

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(stored().getCapturedAmount()).isEqualByComparingTo("4290");
        assertThat(announced()).singleElement().satisfies(event -> {
            assertThat(event.getShipmentId()).isEqualTo(TestFixtures.SHIPMENT_ID);
            assertThat(event.getStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(event.getAmount()).isEqualByComparingTo("4290");
            assertThat(event.getCurrencyCode()).isEqualTo("HUF");
        });
    }

    @Test
    void finalPriceAboveTheAuthorizedAmountCapturesOnlyTheAuthorizedAmount() {
        payments.save(TestFixtures.authorizedPayment());
        when(stripeGateway.capture(any(), any())).thenReturn(received(499_000));

        publish(TestFixtures.shipmentPriced().finalAmount(new BigDecimal("5890")).build());

        verify(stripeGateway).capture(any(), argThat(amount -> amount.compareTo(new BigDecimal("4990")) == 0));
        assertThat(stored().getCapturedAmount()).isEqualByComparingTo("4990");
    }

    @Test
    void redeliveredPriceIsCapturedOnlyOnce() {
        payments.save(TestFixtures.authorizedPayment());
        when(stripeGateway.capture(any(), any())).thenReturn(received(429_000));
        ShipmentPricedEventDTO priced = TestFixtures.shipmentPriced().build();

        publish(priced);
        publish(priced);

        verify(stripeGateway, times(1)).capture(any(), any());
        assertThat(announced()).hasSize(1);
    }

    @Test
    void priceForAnUnauthorizedPaymentIsNotCaptured() {
        payments.save(TestFixtures.pendingPayment());

        publish(TestFixtures.shipmentPriced().build());

        verifyNoInteractions(stripeGateway);
        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(announced()).isEmpty();
    }

    @Test
    void priceInAnotherCurrencyIsNotCaptured() {
        payments.save(TestFixtures.authorizedPayment());

        publish(TestFixtures.shipmentPriced().finalAmount(new BigDecimal("11.50")).currencyCode("EUR").build());

        verifyNoInteractions(stripeGateway);
        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(announced()).isEmpty();
    }

    @Test
    void failedStripeCaptureIsRetriedAndThenPaid() {
        payments.save(TestFixtures.authorizedPayment());
        when(stripeGateway.capture(any(), any()))
                .thenThrow(new StripeOperationException("Stripe is unreachable", null))
                .thenReturn(received(429_000));

        publish(TestFixtures.shipmentPriced().build());

        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(announced()).hasSize(1);
    }

    /** Stripe's webhook about the capture commits first, so the consumer hits the optimistic lock and retries. */
    @Test
    void captureOvertakenByItsWebhookIsAnnouncedOnlyOnce() {
        Payment payment = payments.save(TestFixtures.authorizedPayment());
        String webhook = stripeWebhooks.paymentIntentEvent("evt_3QkT9aLx2eZvKYlo0s1cKp7e", "payment_intent.succeeded",
                TestFixtures.SHIPMENT_ID, payment.getPaymentIntentId(), Map.of("amount_received", 429_000));
        when(stripeGateway.capture(any(), any())).thenAnswer(invocation -> {
            // on its own thread, so that it runs in its own transaction
            CompletableFuture.runAsync(() -> deliver(webhook)).get(10, TimeUnit.SECONDS);
            return received(429_000);
        });

        publish(TestFixtures.shipmentPriced().build());

        verify(stripeGateway, times(1)).capture(any(), any());
        assertThat(stored().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(stored().getVersion()).isEqualTo(payment.getVersion() + 1);
        assertThat(announced()).hasSize(1);
    }

    private void deliver(String webhook) {
        try {
            mockMvc.perform(stripeWebhooks.delivery(webhook)).andExpect(status().isOk());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void publish(ShipmentPricedEventDTO priced) {
        kafka.sendAndAwaitConsumption("shipment-priced-topic", priced.getShipmentId().toString(), priced);
    }

    private Payment stored() {
        return payments.findByShipmentId(TestFixtures.SHIPMENT_ID).orElseThrow();
    }

    private List<PaymentStatusEventDTO> announced() {
        return kafka.received("payment-status-topic", PaymentStatusEventDTO.class);
    }

    private static PaymentIntent received(long amountMinor) {
        PaymentIntent intent = new PaymentIntent();
        intent.setAmountReceived(amountMinor);
        return intent;
    }
}

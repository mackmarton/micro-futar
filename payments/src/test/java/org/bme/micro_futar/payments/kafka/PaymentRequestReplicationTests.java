package org.bme.micro_futar.payments.kafka;

import org.bme.micro_futar.payments.IntegrationTest;
import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.repositories.PaymentRepository;
import org.bme.micro_futar.payments.support.KafkaTestClient;
import org.bme.micro_futar.payments.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.bme.micro_futar.shared.utils.PaymentTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class PaymentRequestReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private PaymentRepository payments;

    @Test
    void paymentRequestCreatesAPendingPayment() {
        publish(TestFixtures.paymentRequest().build());

        assertThat(payments.findAll()).singleElement().satisfies(payment -> {
            assertThat(payment.getShipmentId()).isEqualTo(TestFixtures.SHIPMENT_ID);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getMaxAmount()).isEqualByComparingTo("4990");
            assertThat(payment.getCurrencyCode()).isEqualTo("HUF");
            assertThat(payment.getSenderEmail()).isEqualTo(TestFixtures.SENDER_EMAIL);
            assertThat(payment.getPaymentTokenHash()).isEqualTo(PaymentTokens.hash(TestFixtures.PAYMENT_TOKEN));
        });
    }

    @Test
    void redeliveredRequestDoesNotCreateASecondPayment() {
        PaymentRequestedEventDTO request = TestFixtures.paymentRequest().build();

        publish(request);
        publish(request);

        assertThat(payments.findAll()).hasSize(1);
    }

    @Test
    void lateRequestLeavesAnAuthorizedPaymentUntouched() {
        payments.save(TestFixtures.authorizedPayment());

        publish(TestFixtures.paymentRequest().build());

        assertThat(payments.findAll()).singleElement()
                .extracting(Payment::getStatus).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    private void publish(PaymentRequestedEventDTO request) {
        kafka.sendAndAwaitConsumption("payment-request-topic", request.getShipmentId().toString(), request);
    }
}

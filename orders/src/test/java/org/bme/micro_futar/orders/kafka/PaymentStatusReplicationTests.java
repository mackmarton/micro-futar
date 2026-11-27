package org.bme.micro_futar.orders.kafka;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class PaymentStatusReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRepository shipments;

    @Test
    void authorizedPaymentConfirmsTheShipmentAndAnnouncesIt() {
        Long id = givenShipmentWithPayment(PaymentStatus.PENDING);

        publish(id, PaymentStatus.AUTHORIZED, "4990");

        assertThat(stored(id).getPaymentStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(stored(id).isConfirmed()).isTrue();
        assertThat(kafka.received("shipment-topic", ShipmentDTO.class)).singleElement()
                .satisfies(shipment -> assertThat(shipment.isConfirmed()).isTrue());
        assertThat(kafka.received("shipment-notification-topic", ShipmentNotificationEventDTO.class))
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.getType()).isEqualTo(NotificationType.SHIPMENT_CREATED);
                    assertThat(notification.getDedupKey()).isEqualTo("SHIPMENT_CREATED:shipment:" + id);
                });
    }

    @Test
    void redeliveredAuthorizationIsAnnouncedOnlyOnce() {
        Long id = givenShipmentWithPayment(PaymentStatus.PENDING);

        publish(id, PaymentStatus.AUTHORIZED, "4990");
        publish(id, PaymentStatus.AUTHORIZED, "4990");

        assertThat(kafka.received("shipment-topic")).hasSize(1);
        assertThat(kafka.received("shipment-notification-topic")).hasSize(1);
    }

    @Test
    void capturedPaymentStoresTheFinalPrice() {
        Long id = givenShipmentWithPayment(PaymentStatus.AUTHORIZED);

        publish(id, PaymentStatus.PAID, "4290");

        assertThat(stored(id).getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(stored(id).getPrice()).isEqualTo(4290.0);
        assertThat(kafka.received("shipment-topic", ShipmentDTO.class)).singleElement()
                .extracting(ShipmentDTO::getPrice).isEqualTo(4290.0);
    }

    @Test
    void failureAfterAuthorizationCancelsTheShipment() {
        Long id = givenShipmentWithPayment(PaymentStatus.AUTHORIZED);

        publish(id, PaymentStatus.FAILED, null);

        assertThat(stored(id).isCanceled()).isTrue();
        assertThat(stored(id).isConfirmed()).isFalse();
        assertThat(kafka.received("shipment-topic", ShipmentDTO.class)).singleElement()
                .satisfies(shipment -> assertThat(shipment.isCanceled()).isTrue());
    }

    @Test
    void expiredCheckoutLeavesTheShipmentUnannounced() {
        Long id = givenShipmentWithPayment(PaymentStatus.PENDING);

        publish(id, PaymentStatus.EXPIRED, null);

        assertThat(stored(id).getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(stored(id).isCanceled()).isFalse();
        assertThat(kafka.received("shipment-topic")).isEmpty();
        assertThat(kafka.received("shipment-notification-topic")).isEmpty();
    }

    @Test
    void refundIsRecorded() {
        Long id = givenShipmentWithPayment(PaymentStatus.PAID);

        publish(id, PaymentStatus.REFUNDED, "4290");

        assertThat(stored(id).getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    private Long givenShipmentWithPayment(PaymentStatus status) {
        Shipment shipment = TestFixtures.pendingShipment();
        shipment.setPaymentStatus(status);
        shipment.setConfirmed(status == PaymentStatus.AUTHORIZED || status == PaymentStatus.PAID);
        return shipments.save(shipment).getId();
    }

    private Shipment stored(Long id) {
        return shipments.findById(id).orElseThrow();
    }

    private void publish(Long shipmentId, PaymentStatus status, String amount) {
        var event = PaymentStatusEventDTO.of(shipmentId, status,
                amount == null ? null : new BigDecimal(amount), "HUF");
        kafka.sendAndAwaitConsumption("payment-status-topic", shipmentId.toString(), event);
    }
}

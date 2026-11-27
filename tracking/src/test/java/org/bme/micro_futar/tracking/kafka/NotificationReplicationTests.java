package org.bme.micro_futar.tracking.kafka;

import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.bme.micro_futar.tracking.IntegrationTest;
import org.bme.micro_futar.tracking.entities.Notification;
import org.bme.micro_futar.tracking.enums.NotificationAudience;
import org.bme.micro_futar.tracking.enums.NotificationStatus;
import org.bme.micro_futar.tracking.repositories.NotificationRepository;
import org.bme.micro_futar.tracking.support.KafkaTestClient;
import org.bme.micro_futar.tracking.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@IntegrationTest
class NotificationReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private NotificationRepository notifications;

    @Test
    void createdShipmentNotifiesSenderAndRecipientOnce() {
        publish(creation());

        assertThat(notifications.findAll())
                .extracting(Notification::getAudience, Notification::getToEmail, Notification::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(NotificationAudience.SENDER, TestFixtures.SENDER_EMAIL, NotificationStatus.PENDING),
                        tuple(NotificationAudience.RECIPIENT, TestFixtures.RECIPIENT_EMAIL, NotificationStatus.PENDING));
    }

    @Test
    void redeliveredEventDoesNotNotifyAgain() {
        var event = creation();
        publish(event);

        publish(event);

        assertThat(notifications.findAll())
                .extracting(Notification::getAudience)
                .containsExactlyInAnyOrder(NotificationAudience.SENDER, NotificationAudience.RECIPIENT);
    }

    @ParameterizedTest
    @EnumSource(value = NotificationType.class, mode = EnumSource.Mode.EXCLUDE, names = "SHIPMENT_CREATED")
    void laterStatusEventsNotifyOnlyTheRecipient(NotificationType type) {
        publish(ShipmentNotificationEventDTO.forShipment(type, TestFixtures.shipment().build()));

        assertThat(notifications.findAll())
                .extracting(Notification::getType, Notification::getAudience, Notification::getToEmail)
                .containsExactly(tuple(type, NotificationAudience.RECIPIENT, TestFixtures.RECIPIENT_EMAIL));
    }

    @Test
    void eachFailedDeliveryAttemptIsNotifiedSeparately() {
        var firstAttempt = failedAttempt(1L);

        publish(firstAttempt);
        publish(firstAttempt);
        publish(failedAttempt(2L));

        assertThat(notifications.findAll())
                .extracting(Notification::getDedupKey)
                .containsExactlyInAnyOrder(
                        "DELIVERY_FAILED:assignment:1:RECIPIENT",
                        "DELIVERY_FAILED:assignment:2:RECIPIENT");
    }

    @Test
    void addresseeWithoutEmailAddressIsSkipped() {
        var event = creation();
        event.setSenderEmail(" ");

        publish(event);

        assertThat(notifications.findAll())
                .extracting(Notification::getAudience)
                .containsExactly(NotificationAudience.RECIPIENT);
    }

    private static ShipmentNotificationEventDTO creation() {
        return ShipmentNotificationEventDTO.forShipment(NotificationType.SHIPMENT_CREATED,
                TestFixtures.shipment().build());
    }

    private static ShipmentNotificationEventDTO failedAttempt(long assignmentId) {
        return ShipmentNotificationEventDTO.forAssignment(NotificationType.DELIVERY_FAILED, assignmentId,
                TestFixtures.shipment().build());
    }

    private void publish(ShipmentNotificationEventDTO event) {
        kafka.sendAndAwaitConsumption("shipment-notification-topic", event.getShipmentId().toString(), event);
    }
}

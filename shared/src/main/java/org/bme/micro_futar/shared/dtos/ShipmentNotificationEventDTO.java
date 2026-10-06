package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.NotificationType;

import java.time.ZonedDateTime;

/**
 * Domain event published when something happens to a shipment that its sender or recipient should be notified
 * about. Unlike the entity topics, every message is a single occurrence, identified by a deterministic
 * {@code dedupKey}: redelivering the same occurrence yields the same key, while a new occurrence (e.g. another
 * failed delivery attempt, which belongs to a new courier assignment) yields a new one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentNotificationEventDTO {
    private String dedupKey;
    private NotificationType type;
    private Long shipmentId;
    private String parcelNumber;
    private String senderName;
    private String senderEmail;
    private String recipientName;
    private String recipientEmail;
    private String recipientAddress;
    private ZonedDateTime occurredAt;

    /** For events that can happen only once per shipment (creation, successful delivery). */
    public static ShipmentNotificationEventDTO forShipment(NotificationType type, ShipmentDTO shipment) {
        return of(type, type + ":shipment:" + shipment.getId(), shipment);
    }

    /** For events that happen once per courier assignment (pickup for delivery, failed delivery attempt). */
    public static ShipmentNotificationEventDTO forAssignment(NotificationType type, Long shipmentRouteCourierId,
                                                             ShipmentDTO shipment) {
        return of(type, type + ":assignment:" + shipmentRouteCourierId, shipment);
    }

    private static ShipmentNotificationEventDTO of(NotificationType type, String dedupKey, ShipmentDTO shipment) {
        return ShipmentNotificationEventDTO.builder()
                .dedupKey(dedupKey)
                .type(type)
                .shipmentId(shipment.getId())
                .parcelNumber(shipment.getParcelNumber())
                .senderName(shipment.getSenderName())
                .senderEmail(shipment.getSenderEmail())
                .recipientName(shipment.getRecipientName())
                .recipientEmail(shipment.getRecipientEmail())
                .recipientAddress(shipment.getRecipientAddress())
                .occurredAt(ZonedDateTime.now())
                .build();
    }
}

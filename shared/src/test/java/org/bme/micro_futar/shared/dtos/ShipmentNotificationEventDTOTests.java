package org.bme.micro_futar.shared.dtos;

import org.bme.micro_futar.shared.enums.NotificationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentNotificationEventDTOTests {

    private static ShipmentDTO shipment() {
        ShipmentDTO shipment = new ShipmentDTO();
        shipment.setId(42L);
        return shipment;
    }

    @Test
    void eventThatHappensOncePerShipmentIsKeyedByTheShipment() {
        var event = ShipmentNotificationEventDTO.forShipment(NotificationType.DELIVERED, shipment());

        assertThat(event.getDedupKey()).isEqualTo("DELIVERED:shipment:42");
    }

    @Test
    void everyCourierAssignmentGetsItsOwnKey() {
        var firstAttempt = ShipmentNotificationEventDTO.forAssignment(NotificationType.DELIVERY_FAILED, 7L, shipment());
        var secondAttempt = ShipmentNotificationEventDTO.forAssignment(NotificationType.DELIVERY_FAILED, 8L, shipment());

        assertThat(firstAttempt.getDedupKey()).isEqualTo("DELIVERY_FAILED:assignment:7");
        assertThat(secondAttempt.getDedupKey()).isEqualTo("DELIVERY_FAILED:assignment:8");
    }
}

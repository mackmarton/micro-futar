package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * Publishes customer-facing notification events for the last-mile leg of a shipment (the route part that ends at
 * the recipient's address). Best effort: a failure is logged and never fails the courier's action.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentNotificationService {

    private final ShipmentService shipmentService;
    private final KafkaProducerService kafkaProducerService;

    public void notifyOutForDelivery(ShipmentRouteDTO shipmentRoute, Long shipmentRouteCourierId) {
        notifyForAssignment(NotificationType.OUT_FOR_DELIVERY, shipmentRoute, shipmentRouteCourierId);
    }

    public void notifyDeliveryFailed(ShipmentRouteDTO shipmentRoute, Long shipmentRouteCourierId) {
        notifyForAssignment(NotificationType.DELIVERY_FAILED, shipmentRoute, shipmentRouteCourierId);
    }

    public void notifyDelivered(ShipmentRouteDTO shipmentRoute) {
        if (!isDeliveryLeg(shipmentRoute)) {
            return;
        }
        publish(shipmentRoute, shipment -> ShipmentNotificationEventDTO.forShipment(NotificationType.DELIVERED, shipment));
    }

    private void notifyForAssignment(NotificationType type, ShipmentRouteDTO shipmentRoute, Long shipmentRouteCourierId) {
        if (!isDeliveryLeg(shipmentRoute)) {
            return;
        }
        publish(shipmentRoute, shipment -> ShipmentNotificationEventDTO.forAssignment(type, shipmentRouteCourierId, shipment));
    }

    private void publish(ShipmentRouteDTO shipmentRoute, Function<ShipmentDTO, ShipmentNotificationEventDTO> eventFactory) {
        try {
            shipmentService.findById(shipmentRoute.getShipmentId())
                    .map(eventFactory)
                    .ifPresentOrElse(kafkaProducerService::sendShipmentNotification,
                            () -> log.warn("Shipment {} not found, skipping notification for shipmentRoute {}",
                                    shipmentRoute.getShipmentId(), shipmentRoute.getId()));
        } catch (Exception e) {
            log.error("Failed to publish notification for shipmentRoute {}", shipmentRoute.getId(), e);
        }
    }

    private static boolean isDeliveryLeg(ShipmentRouteDTO shipmentRoute) {
        return shipmentRoute.getDestinationAddress() != null;
    }
}

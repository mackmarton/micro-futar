package org.bme.micro_futar.tracking.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.tracking.services.NotificationService;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentNotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // Separate consumer group, so notification handling never holds up the read-model consumers.
    @KafkaListener(topics = "${kafka.topics.shipment-notification-topic}", groupId = "tracking-notification-group")
    public void consumeShipmentNotification(String message) {
        log.info("Received shipment notification message: {}", message);
        try {
            ShipmentNotificationEventDTO event = objectMapper.readValue(message, ShipmentNotificationEventDTO.class);
            notificationService.enqueue(event);
        } catch (Exception e) {
            log.error("Error processing shipment notification message: {}", message, e);
            throw new KafkaException("Error processing shipment notification message", e);
        }
    }
}

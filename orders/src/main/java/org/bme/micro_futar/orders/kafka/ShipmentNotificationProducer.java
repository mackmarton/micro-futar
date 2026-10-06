package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentNotificationProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.shipment-notification-topic}")
    private String shipmentNotificationTopic;

    /**
     * Notifications are best effort: a failure here is logged and must not roll back the shipment itself.
     */
    public void sendNotificationToTopic(ShipmentNotificationEventDTO event) {
        log.info("Sending shipment notification event to Kafka: {}", event.getDedupKey());
        try {
            String eventJSON = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(shipmentNotificationTopic, event.getShipmentId().toString(), eventJSON)
                    .whenComplete((_, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send shipment notification event to Kafka: {}", event.getDedupKey(), ex);
                        } else {
                            log.info("Successfully sent shipment notification event: {}", event.getDedupKey());
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending shipment notification event to Kafka: {}", event.getDedupKey(), e);
        }
    }
}

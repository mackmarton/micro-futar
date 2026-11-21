package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentDeliveredEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
import org.bme.micro_futar.shared.exceptions.KafkaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.shipment-delivered-topic}")
    private String shipmentDeliveredTopic;
    @Value("${kafka.topics.shipment-route-fulfilled-topic}")
    private String shipmentRouteFulfilledTopic;
    @Value("${kafka.topics.assignment-status-topic}")
    private String assignmentStatusTopic;
    @Value("${kafka.send-timeout-seconds:10}")
    private long sendTimeoutSeconds;
    @Value("${kafka.topics.shipment-notification-topic}")
    private String shipmentNotificationTopic;

    public void sendShipmentRouteFulfilled(ShipmentRouteFulfilledEventDTO event) {
        sendAndWait(shipmentRouteFulfilledTopic, event.getShipmentRouteId().toString(), event, "shipment route fulfilled");
    }

    public void sendAssignmentStatus(AssignmentStatusEventDTO event) {
        sendAndWait(assignmentStatusTopic, event.getAssignmentId().toString(), event, "assignment status");
    }

    public void sendShipmentDelivered(ShipmentDeliveredEventDTO event) {
        sendAndWait(shipmentDeliveredTopic, event.getShipmentId().toString(), event, "shipment delivered");
    }

    private void sendAndWait(String topic, String key, Object event, String what) {
        log.info("Sending {} event to topic {}: {}", what, topic, event);
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, jsonMessage).get(sendTimeoutSeconds, TimeUnit.SECONDS);
            log.info("Successfully sent {} event with key {} to topic: {}", what, key, topic);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sending {} event with key {} to topic: {}", what, key, topic, e);
            throw new KafkaException("Interrupted while sending " + what + " event", e);
        } catch (Exception e) {
            log.error("Failed to send {} event with key {} to topic: {}", what, key, topic, e);
            throw new KafkaException("Failed to send " + what + " event", e);
        }
    }

    /** Best effort: a lost notification never fails the courier's action. */
    public void sendShipmentNotification(ShipmentNotificationEventDTO event) {
        log.info("Sending shipment notification event to topic {}: {}", shipmentNotificationTopic, event.getDedupKey());
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(shipmentNotificationTopic, event.getShipmentId().toString(), jsonMessage)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent shipment notification event {} to topic: {}",
                                    event.getDedupKey(), shipmentNotificationTopic);
                        } else {
                            log.error("Failed to send shipment notification event {} to topic: {}",
                                    event.getDedupKey(), shipmentNotificationTopic, ex);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Error serializing shipment notification event to JSON", e);
            throw new KafkaException("Failed to serialize shipment notification event", e);
        } catch (Exception e) {
            log.error("Error sending shipment notification event to Kafka", e);
            throw new KafkaException("Failed to send shipment notification event", e);
        }
    }
}

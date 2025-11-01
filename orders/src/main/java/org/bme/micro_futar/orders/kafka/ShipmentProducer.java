package org.bme.micro_futar.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentProducer {

    private final KafkaTemplate<String, ShipmentDTO> kafkaTemplate;

    @Value("${kafka.topics.shipment-topic}")
    private String shipmentTopic;

    @Transactional
    public void sendShipmentToTopic(ShipmentDTO shipmentDTO) {
        log.info("Sending shipment message to Kafka: {}", shipmentDTO);
        try {
            String key = shipmentDTO.getId() != null ? shipmentDTO.getId().toString() : "";
            CompletableFuture<SendResult<String, ShipmentDTO>> future = kafkaTemplate.send(shipmentTopic, key, shipmentDTO);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send shipment message to Kafka: {}", shipmentDTO, ex);
                } else {
                    log.info("Successfully sent shipment message with ID: {} to partition: {}",
                            shipmentDTO.getId(), result.getRecordMetadata().partition());
                }
            });
        } catch (Exception e) {
            log.error("Error sending shipment message to Kafka: {}", shipmentDTO, e);
            throw e;
        }
    }
}

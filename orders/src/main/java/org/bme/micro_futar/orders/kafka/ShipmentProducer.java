package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.exceptions.KafkaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.shipment-topic}")
    private String shipmentTopic;

    @Transactional
    public void sendShipmentToTopic(ShipmentDTO shipmentDTO) {
        log.info("Sending shipment message to Kafka: {}", shipmentDTO);
        try {
            String key = shipmentDTO.getId() != null ? shipmentDTO.getId().toString() : "";
            String shipmentDTOJSON = objectMapper.writeValueAsString(shipmentDTO);
            kafkaTemplate.send(shipmentTopic, key, shipmentDTOJSON)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send shipment message to Kafka: {}", shipmentDTO, ex);
                        } else {
                            log.info("Successfully sent shipment message with ID: {} to partition: {}",
                                    shipmentDTO.getId(), result.getRecordMetadata().partition());
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending shipment message to Kafka: {}", shipmentDTO, e);
            throw new KafkaException("Failed to send shipment message", e);
        }
    }
}

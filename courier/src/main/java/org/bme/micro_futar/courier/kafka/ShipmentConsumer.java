package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentConsumer {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-topic}", groupId = "shipment-group")
    public void consumeShipment(String message) {
        log.info("Received shipment message: {}", message);
        try {
            ShipmentDTO shipmentDTO = objectMapper.readValue(message, ShipmentDTO.class);
            shipmentService.save(shipmentDTO);
            log.info("Successfully processed shipment: {}", shipmentDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipment message: {}", message, e);
            throw new KafkaException("Failed to process shipment message", e);
        }
    }
}


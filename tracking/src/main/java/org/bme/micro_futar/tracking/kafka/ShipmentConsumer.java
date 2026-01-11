package org.bme.micro_futar.tracking.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.tracking.services.ShipmentService;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentConsumer {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-topic}", groupId = "tracking-group")
    public void consumeShipment(String message) {
        log.info("Received shipment message: {}", message);
        try {
            ShipmentDTO shipmentDTO = objectMapper.readValue(message, ShipmentDTO.class);
            shipmentService.save(shipmentDTO);
            log.info("Successfully processed shipment: {}", shipmentDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipment message: {}", message, e);
            throw new KafkaException("Error processing shipment message",e);
        }
    }
}


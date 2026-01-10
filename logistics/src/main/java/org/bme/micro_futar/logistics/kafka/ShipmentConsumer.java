package org.bme.micro_futar.logistics.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentConsumer {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-topic}", groupId = "logistics-group")
    public void consumeShipment(String message) {
        log.info("Received shipment message: {}", message);
        try {
            ShipmentDTO shipmentDTO = objectMapper.readValue(message, ShipmentDTO.class);
            shipmentService.processShipment(shipmentDTO);
            log.info("Successfully processed shipment with ID: {}", shipmentDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipment message: {}", message, e);
        }
    }
}

package org.bme.micro_futar.logistics.kafka;

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

    @KafkaListener(topics = "${kafka.topics.shipment-topic}", groupId = "logistics-group")
    public void consumeShipment(ShipmentDTO shipmentDTO) {
        log.info("Received shipment message: {}", shipmentDTO);
        try {
            shipmentService.processShipment(shipmentDTO);
            log.info("Successfully processed shipment with ID: {}", shipmentDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipment with ID: {}", shipmentDTO.getId(), e);
        }
    }
}

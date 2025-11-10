package org.bme.micro_futar.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentConsumer {

    private final ShipmentService shipmentService;

    @KafkaListener(topics = "${kafka.topics.shipment-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeShipment(ShipmentDTO shipmentDTO) {
        log.info("Received shipment message: {}", shipmentDTO);
        try {
            shipmentService.save(shipmentDTO);
            log.info("Successfully processed shipment: {}", shipmentDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipment message: {}", shipmentDTO, e);
            throw e;
        }
    }
}


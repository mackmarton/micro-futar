package org.bme.micro_futar.logistics.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.services.DepoShipmentService;
import org.bme.micro_futar.shared.dtos.DepoShipmentDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepoShipmentConsumer {

    private final DepoShipmentService depoShipmentService;

    @KafkaListener(topics = "${kafka.topics.depo-shipment-topic}", groupId = "logistics-group")
    public void consumeDepoShipment(DepoShipmentDTO depoShipmentDTO) {
        log.info("Received depo shipment message: {}", depoShipmentDTO);
        try {
            depoShipmentService.saveDepoShipment(depoShipmentDTO);
            log.info("Successfully processed depo shipment with ID: {}", depoShipmentDTO.getId());
        } catch (Exception e) {
            log.error("Error processing depo shipment with ID: {}", depoShipmentDTO.getId(), e);
        }
    }
}

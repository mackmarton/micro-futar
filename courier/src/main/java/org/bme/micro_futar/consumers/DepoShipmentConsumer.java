package org.bme.micro_futar.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.DepoShipmentService;
import org.bme.micro_futar.shared.dtos.DepoShipmentDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepoShipmentConsumer {

    private final DepoShipmentService depoShipmentService;

    @KafkaListener(topics = "${kafka.topics.depo-shipment-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeDepoShipment(DepoShipmentDTO depoShipmentDTO) {
        log.info("Received depoShipment message: {}", depoShipmentDTO);
        try {
            depoShipmentService.save(depoShipmentDTO);
            log.info("Successfully processed depoShipment: {}", depoShipmentDTO.getId());
        } catch (Exception e) {
            log.error("Error processing depoShipment message: {}", depoShipmentDTO, e);
            throw e;
        }
    }
}

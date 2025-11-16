package org.bme.micro_futar.logistics.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.services.ShipmentRouteCourierService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentRouteCourierConsumer {

    private final ShipmentRouteCourierService shipmentRouteCourierService;

    @KafkaListener(topics = "${kafka.topics.shipment-route-courier-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeShipmentRouteCourier(ShipmentRouteCourierDTO shipmentRouteCourierDTO) {
        log.info("Received shipmentRouteCourier message: {}", shipmentRouteCourierDTO);
        try {
            shipmentRouteCourierService.saveWithoutTopicSend(shipmentRouteCourierDTO);
            log.info("Successfully processed shipmentRouteCourier: {}", shipmentRouteCourierDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipmentRouteCourier message: {}", shipmentRouteCourierDTO, e);
            throw e;
        }
    }
}


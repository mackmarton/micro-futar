package org.bme.micro_futar.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.ShipmentRouteService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentRouteConsumer {

    private final ShipmentRouteService shipmentRouteService;

    @KafkaListener(topics = "${kafka.topics.shipment-route-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeShipmentRoute(ShipmentRouteDTO shipmentRouteDTO) {
        log.info("Received shipmentRoute message: {}", shipmentRouteDTO);
        try {
            shipmentRouteService.save(shipmentRouteDTO);
            log.info("Successfully processed shipmentRoute: {}", shipmentRouteDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipmentRoute message: {}", shipmentRouteDTO, e);
            throw e;
        }
    }
}


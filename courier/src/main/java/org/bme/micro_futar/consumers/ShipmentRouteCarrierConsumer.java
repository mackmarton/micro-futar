package org.bme.micro_futar.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.ShipmentRouteCarrierService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCarrierDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentRouteCarrierConsumer {

    private final ShipmentRouteCarrierService shipmentRouteCarrierService;

    @KafkaListener(topics = "${kafka.topics.shipment-route-carrier-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeShipmentRouteCarrier(ShipmentRouteCarrierDTO shipmentRouteCarrierDTO) {
        log.info("Received shipmentRouteCarrier message: {}", shipmentRouteCarrierDTO);
        try {
            shipmentRouteCarrierService.save(shipmentRouteCarrierDTO);
            log.info("Successfully processed shipmentRouteCarrier: {}", shipmentRouteCarrierDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipmentRouteCarrier message: {}", shipmentRouteCarrierDTO, e);
            throw e;
        }
    }
}


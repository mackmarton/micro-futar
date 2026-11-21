package org.bme.micro_futar.logistics.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.services.ShipmentRouteService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentRouteFulfilledConsumer {

    private final ShipmentRouteService shipmentRouteService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-route-fulfilled-topic}", groupId = "logistics-shipment-route-fulfilled")
    public void consumeShipmentRouteFulfilled(String message) {
        log.info("Received shipmentRoute fulfilled message: {}", message);
        try {
            shipmentRouteService.applyFulfilled(objectMapper.readValue(message, ShipmentRouteFulfilledEventDTO.class));
        } catch (Exception e) {
            log.error("Error processing shipmentRoute fulfilled message: {}", message, e);
            throw new KafkaException("Failed to process shipmentRoute fulfilled message", e);
        }
    }
}

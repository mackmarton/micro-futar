package org.bme.micro_futar.tracking.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.tracking.services.ShipmentRouteService;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentRouteConsumer {

    private final ShipmentRouteService shipmentRouteService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-route-topic}", groupId = "tracking-group")
    public void consumeShipmentRoute(String message) {
        log.info("Received shipmentRoute message: {}", message);
        try {
            ShipmentRouteDTO shipmentRouteDTO = objectMapper.readValue(message, ShipmentRouteDTO.class);
            shipmentRouteService.save(shipmentRouteDTO);
            log.info("Successfully processed shipmentRoute: {}", shipmentRouteDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipmentRoute message: {}", message, e);
            throw new KafkaException("Error processing shipmentRoute message", e);
        }
    }
}


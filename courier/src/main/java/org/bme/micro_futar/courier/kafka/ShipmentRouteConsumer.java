package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.ShipmentRouteService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentRouteConsumer {

    private final ShipmentRouteService shipmentRouteService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-route-topic}", groupId = "shipment-route-group")
    public void consumeShipmentRoute(String message) {
        log.info("Received shipmentRoute message: {}", message);
        try {
            ShipmentRouteDTO shipmentRouteDTO = objectMapper.readValue(message, ShipmentRouteDTO.class);
            shipmentRouteService.saveWithoutTopicSend(shipmentRouteDTO);
            log.info("Successfully processed shipmentRoute: {}", shipmentRouteDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipmentRoute message: {}", message, e);
            throw new KafkaException("Failed to process shipmentRoute message", e);
        }
    }
}


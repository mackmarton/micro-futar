package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.ShipmentDeliveredEventDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentDeliveredConsumer {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-delivered-topic}", groupId = "orders-shipment-delivered")
    public void consumeShipmentDelivered(String message) {
        log.info("Received shipment delivered message: {}", message);
        try {
            ShipmentDeliveredEventDTO event = objectMapper.readValue(message, ShipmentDeliveredEventDTO.class);
            shipmentService.applyDelivered(event);
        } catch (Exception e) {
            log.error("Error processing shipment delivered message: {}", message, e);
            throw new KafkaException("Failed to process shipment delivered message", e);
        }
    }
}

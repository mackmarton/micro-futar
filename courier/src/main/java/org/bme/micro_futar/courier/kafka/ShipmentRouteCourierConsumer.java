package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.ShipmentRouteCourierService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentRouteCourierConsumer {

    private final ShipmentRouteCourierService shipmentRouteCourierService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-route-courier-topic}", groupId = "shipment-route-courier-group")
    public void consumeShipmentRouteCourier(String message) {
        log.info("Received shipmentRouteCourier message: {}", message);
        try {
            ShipmentRouteCourierDTO shipmentRouteCourierDTO = objectMapper.readValue(message, ShipmentRouteCourierDTO.class);
            shipmentRouteCourierService.saveWithoutTopicSend(shipmentRouteCourierDTO);
            log.info("Successfully processed shipmentRouteCourier: {}", shipmentRouteCourierDTO.getId());
        } catch (Exception e) {
            log.error("Error processing shipmentRouteCourier message: {}", message, e);
            throw new KafkaException("Failed to process shipmentRouteCourier message", e);
        }
    }
}


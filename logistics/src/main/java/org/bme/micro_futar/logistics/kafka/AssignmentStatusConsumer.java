package org.bme.micro_futar.logistics.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.services.ShipmentRouteCourierService;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentStatusConsumer {

    private final ShipmentRouteCourierService shipmentRouteCourierService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.assignment-status-topic}", groupId = "logistics-assignment-status")
    public void consumeAssignmentStatus(String message) {
        log.info("Received assignment status message: {}", message);
        try {
            shipmentRouteCourierService.applyStatus(objectMapper.readValue(message, AssignmentStatusEventDTO.class));
        } catch (Exception e) {
            log.error("Error processing assignment status message: {}", message, e);
            throw new KafkaException("Failed to process assignment status message", e);
        }
    }
}

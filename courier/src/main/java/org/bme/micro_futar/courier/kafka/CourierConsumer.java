package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.CourierService;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourierConsumer {

    private final CourierService courierService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.courier-topic}", groupId = "courier-entity-group")
    public void consumeCourier(String message) {
        log.info("Received courier message: {}", message);
        try {
            CourierDTO courierDTO = objectMapper.readValue(message, CourierDTO.class);
            courierService.save(courierDTO);
            log.info("Successfully processed courier: {}", courierDTO.getId());
        } catch (Exception e) {
            log.error("Error processing courier message: {}", message, e);
            throw new KafkaException("Failed to process courier message", e);
        }
    }
}

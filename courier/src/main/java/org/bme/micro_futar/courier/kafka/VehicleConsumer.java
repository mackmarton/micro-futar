package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.VehicleService;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleConsumer {

    private final VehicleService vehicleService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.vehicle-topic}", groupId = "vehicle-group")
    public void consumeVehicle(String message) {
        log.info("Received vehicle message: {}", message);
        try {
            VehicleDTO vehicleDTO = objectMapper.readValue(message, VehicleDTO.class);
            vehicleService.save(vehicleDTO);
            log.info("Successfully processed vehicle: {}", vehicleDTO.getId());
        } catch (Exception e) {
            log.error("Error processing vehicle message: {}", message, e);
            throw new KafkaException("Failed to process vehicle message", e);
        }
    }
}

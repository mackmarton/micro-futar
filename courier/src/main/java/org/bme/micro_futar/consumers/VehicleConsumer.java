package org.bme.micro_futar.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.VehicleService;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleConsumer {

    private final VehicleService vehicleService;

    @KafkaListener(topics = "${kafka.topics.vehicle-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeVehicle(VehicleDTO vehicleDTO) {
        log.info("Received vehicle message: {}", vehicleDTO);
        try {
            vehicleService.save(vehicleDTO);
            log.info("Successfully processed vehicle: {}", vehicleDTO.getId());
        } catch (Exception e) {
            log.error("Error processing vehicle message: {}", vehicleDTO, e);
            throw e;
        }
    }
}

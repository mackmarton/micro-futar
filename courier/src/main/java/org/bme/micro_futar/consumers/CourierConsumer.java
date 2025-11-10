package org.bme.micro_futar.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.CourierService;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourierConsumer {

    private final CourierService courierService;

    @KafkaListener(topics = "${kafka.topics.courier-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeCourier(CourierDTO courierDTO) {
        log.info("Received courier message: {}", courierDTO);
        try {
            courierService.save(courierDTO);
            log.info("Successfully processed courier: {}", courierDTO.getId());
        } catch (Exception e) {
            log.error("Error processing courier message: {}", courierDTO, e);
            throw e;
        }
    }
}

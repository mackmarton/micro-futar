package org.bme.micro_futar.courier.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.DepoService;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepoConsumer {

    private final DepoService depoService;

    @KafkaListener(topics = "${kafka.topics.depo-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeDepo(DepoDTO depoDTO) {
        log.info("Received depo message: {}", depoDTO);
        try {
            depoService.save(depoDTO);
            log.info("Successfully processed depo: {}", depoDTO.getId());
        } catch (Exception e) {
            log.error("Error processing depo message: {}", depoDTO, e);
            throw e;
        }
    }
}

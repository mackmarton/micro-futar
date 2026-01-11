package org.bme.micro_futar.tracking.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.tracking.services.DepoService;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepoConsumer {

    private final DepoService depoService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.depo-topic}", groupId = "tracking-group")
    public void consumeDepo(String message) {
        log.info("Received depo message: {}", message);
        try {
            DepoDTO depoDTO = objectMapper.readValue(message, DepoDTO.class);
            depoService.save(depoDTO);
            log.info("Successfully processed depo: {}", depoDTO.getId());
        } catch (Exception e) {
            log.error("Error processing depo message: {}", message, e);
            throw new KafkaException("Error processing depo message", e);
        }
    }
}

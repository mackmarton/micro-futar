package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.PackageSizeService;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PackageSizeConsumer {

    private final PackageSizeService packageSizeService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.package-size-topic}", groupId = "package-size-group")
    public void consumePackageSize(String message) {
        log.info("Received packageSize message: {}", message);
        try {
            PackageSizeDTO packageSizeDTO = objectMapper.readValue(message, PackageSizeDTO.class);
            packageSizeService.save(packageSizeDTO);
            log.info("Successfully processed packageSize: {}", packageSizeDTO.getId());
        } catch (Exception e) {
            log.error("Error processing packageSize message: {}", message, e);
            throw new KafkaException("Failed to process packageSize message", e);
        }
    }
}


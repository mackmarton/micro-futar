package org.bme.micro_futar.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.PackageSizeService;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PackageSizeConsumer {

    private final PackageSizeService packageSizeService;

    @KafkaListener(topics = "${kafka.topics.package-size-topic}", groupId = "courier-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumePackageSize(PackageSizeDTO packageSizeDTO) {
        log.info("Received packageSize message: {}", packageSizeDTO);
        try {
            packageSizeService.save(packageSizeDTO);
            log.info("Successfully processed packageSize: {}", packageSizeDTO.getId());
        } catch (Exception e) {
            log.error("Error processing packageSize message: {}", packageSizeDTO, e);
            throw e;
        }
    }
}


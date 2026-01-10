package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.PackageSize;
import org.bme.micro_futar.orders.services.PackageSizeService;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PackageSizeConsumer {

    private final PackageSizeService packageSizeService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.package-size-topic}", groupId = "orders-service")
    public void consumePackageSize(String message) {
        log.info("Received package size message: {}", message);

        try {
            PackageSizeDTO packageSizeDTO = objectMapper.readValue(message, PackageSizeDTO.class);
            PackageSize packageSize = packageSizeService.savePackageSize(packageSizeDTO);
            log.info("Successfully saved package size with ID: {}", packageSize.getId());
        } catch (Exception e) {
            log.error("Error processing package size message: {}", message, e);
            throw new KafkaException("Failed to process package size message", e);
        }
    }
}

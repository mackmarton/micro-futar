package org.bme.micro_futar.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.PackageSize;
import org.bme.micro_futar.orders.services.PackageSizeService;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PackageSizeConsumer {

    private final PackageSizeService packageSizeService;

    @KafkaListener(topics = "package-size", groupId = "orders-service")
    public void consumePackageSize(PackageSizeDTO packageSizeDTO) {
        log.info("Received package size message: {}", packageSizeDTO);

        try {
            PackageSize packageSize = packageSizeService.savePackageSize(packageSizeDTO);
            log.info("Successfully saved package size with ID: {}", packageSize.getId());
        } catch (Exception e) {
            log.error("Error processing package size message: {}", packageSizeDTO, e);
            throw e;
        }
    }
}

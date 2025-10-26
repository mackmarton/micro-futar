package org.bme.micro_futar.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.LocationRegion;
import org.bme.micro_futar.orders.services.LocationRegionService;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationRegionConsumer {

    private final LocationRegionService locationRegionService;

    @KafkaListener(topics = "${kafka.topics.location-region-topic}", groupId = "orders-service")
    public void consumeLocationRegion(LocationRegionDTO locationRegionDTO) {
        log.info("Received location region message: {}", locationRegionDTO);

        try {
            LocationRegion locationRegion = locationRegionService.saveLocationRegion(locationRegionDTO);
            log.info("Successfully saved location region with ID: {}", locationRegion.getId());
        } catch (Exception e) {
            log.error("Error processing location region message: {}", locationRegionDTO, e);
            throw e;
        }
    }
}

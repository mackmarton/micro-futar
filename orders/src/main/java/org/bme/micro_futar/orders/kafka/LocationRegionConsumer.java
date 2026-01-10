package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.LocationRegion;
import org.bme.micro_futar.orders.services.LocationRegionService;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationRegionConsumer {

    private final LocationRegionService locationRegionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.location-region-topic}", groupId = "orders-service")
    public void consumeLocationRegion(String message) {
        log.info("Received location region message: {}", message);

        try {
            LocationRegionDTO locationRegionDTO = objectMapper.readValue(message, LocationRegionDTO.class);
            LocationRegion locationRegion = locationRegionService.saveLocationRegion(locationRegionDTO);
            log.info("Successfully saved location region with ID: {}", locationRegion.getId());
        } catch (Exception e) {
            log.error("Error processing location region message: {}", message, e);
            throw new KafkaException("Failed to process location region message", e);
        }
    }
}

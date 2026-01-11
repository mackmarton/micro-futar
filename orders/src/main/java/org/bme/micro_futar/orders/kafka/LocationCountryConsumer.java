package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.LocationCountry;
import org.bme.micro_futar.orders.services.LocationCountryService;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationCountryConsumer {

    private final LocationCountryService locationCountryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.location-country-topic}", groupId = "orders-location-country")
    public void consumeLocationCountry(String message) {
        log.info("Received location country message: {}", message);

        try {
            LocationCountryDTO locationCountryDTO = objectMapper.readValue(message, LocationCountryDTO.class);
            LocationCountry locationCountry = locationCountryService.saveLocationCountry(locationCountryDTO);
            log.info("Successfully saved location country with ID: {}", locationCountry.getId());
        } catch (Exception e) {
            log.error("Error processing location country message: {}", message, e);
            throw new KafkaException("Failed to process location country message", e);
        }
    }
}

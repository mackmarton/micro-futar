package org.bme.micro_futar.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.LocationCountry;
import org.bme.micro_futar.orders.services.LocationCountryService;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationCountryConsumer {

    private final LocationCountryService locationCountryService;

    @KafkaListener(topics = "${kafka.topics.location-country-topic}", groupId = "orders-service")
    public void consumeLocationCountry(LocationCountryDTO locationCountryDTO) {
        log.info("Received location country message: {}", locationCountryDTO);

        try {
            LocationCountry locationCountry = locationCountryService.saveLocationCountry(locationCountryDTO);
            log.info("Successfully saved location country with ID: {}", locationCountry.getId());
        } catch (Exception e) {
            log.error("Error processing location country message: {}", locationCountryDTO, e);
            throw e;
        }
    }
}

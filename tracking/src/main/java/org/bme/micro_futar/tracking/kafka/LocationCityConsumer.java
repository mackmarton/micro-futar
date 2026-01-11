package org.bme.micro_futar.tracking.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.tracking.entities.LocationCity;
import org.bme.micro_futar.tracking.services.LocationCityService;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationCityConsumer {

    private final LocationCityService locationCityService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.location-city-topic}", groupId = "tracking-group")
    public void consumeLocationCity(String message) {
        log.info("Received location city message: {}", message);
        try {
            LocationCityDTO locationCityDTO = objectMapper.readValue(message, LocationCityDTO.class);
            LocationCity locationCity = locationCityService.saveLocationCity(locationCityDTO);
            log.info("Successfully saved location city with ID: {}", locationCity.getId());
        } catch (Exception e) {
            log.error("Error processing location city message: {}", message, e);
            throw new KafkaException("Error processing location city message", e);
        }
    }
}

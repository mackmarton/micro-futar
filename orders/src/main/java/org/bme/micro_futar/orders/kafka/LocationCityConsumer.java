package org.bme.micro_futar.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.orders.services.LocationCityService;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationCityConsumer {

    private final LocationCityService locationCityService;

    @KafkaListener(topics = "location-city", groupId = "orders-service")
    public void consumeLocationCity(LocationCityDTO locationCityDTO) {
        log.info("Received location city message: {}", locationCityDTO);

        try {
            LocationCity locationCity = locationCityService.saveLocationCity(locationCityDTO);
            log.info("Successfully saved location city with ID: {}", locationCity.getId());
        } catch (Exception e) {
            log.error("Error processing location city message: {}", locationCityDTO, e);
            throw e;
        }
    }
}

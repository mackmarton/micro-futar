package org.bme.micro_futar.logistics.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.exceptions.KafkaException;
import org.bme.micro_futar.shared.dtos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.location-region-topic}")
    private String locationRegionTopic;

    @Value("${kafka.topics.location-country-topic}")
    private String locationCountryTopic;

    @Value("${kafka.topics.location-city-topic}")
    private String locationCityTopic;

    @Value("${kafka.topics.country-price-topic}")
    private String countryPriceTopic;

    @Value("${kafka.topics.package-size-topic}")
    private String packageSizeTopic;

    public void sendLocationRegion(LocationRegionDTO locationRegionDTO) {
        log.info("Sending location region message to topic {}: {}", locationRegionTopic, locationRegionDTO);
        try {
            kafkaTemplate.send(locationRegionTopic, locationRegionDTO.getId().toString(), locationRegionDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent location region with ID: {} to topic: {}",
                                    locationRegionDTO.getId(), locationRegionTopic);
                        } else {
                            log.error("Failed to send location region with ID: {} to topic: {}",
                                    locationRegionDTO.getId(), locationRegionTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending location region message to Kafka", e);
            throw new KafkaException("Failed to send location region message", e);
        }
    }

    public void sendLocationCountry(LocationCountryDTO locationCountryDTO) {
        log.info("Sending location country message to topic {}: {}", locationCountryTopic, locationCountryDTO);
        try {
            kafkaTemplate.send(locationCountryTopic, locationCountryDTO.getId().toString(), locationCountryDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent location country with ID: {} to topic: {}",
                                    locationCountryDTO.getId(), locationCountryTopic);
                        } else {
                            log.error("Failed to send location country with ID: {} to topic: {}",
                                    locationCountryDTO.getId(), locationCountryTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending location country message to Kafka", e);
            throw new KafkaException("Failed to send location country message", e);
        }
    }

    public void sendLocationCity(LocationCityDTO locationCityDTO) {
        log.info("Sending location city message to topic {}: {}", locationCityTopic, locationCityDTO);
        try {
            kafkaTemplate.send(locationCityTopic, locationCityDTO.getId().toString(), locationCityDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent location city with ID: {} to topic: {}",
                                    locationCityDTO.getId(), locationCityTopic);
                        } else {
                            log.error("Failed to send location city with ID: {} to topic: {}",
                                    locationCityDTO.getId(), locationCityTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending location city message to Kafka", e);
            throw new KafkaException("Failed to send location city message", e);
        }
    }

    public void sendCountryPrice(CountryPriceDTO countryPriceDTO) {
        log.info("Sending country price message to topic {}: {}", countryPriceTopic, countryPriceDTO);
        try {
            kafkaTemplate.send(countryPriceTopic, countryPriceDTO.getId().toString(), countryPriceDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent country price with ID: {} to topic: {}",
                                    countryPriceDTO.getId(), countryPriceTopic);
                        } else {
                            log.error("Failed to send country price with ID: {} to topic: {}",
                                    countryPriceDTO.getId(), countryPriceTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending country price message to Kafka", e);
            throw new KafkaException("Failed to send country price message", e);
        }
    }

    public void sendPackageSize(PackageSizeDTO packageSizeDTO) {
        log.info("Sending package size message to topic {}: {}", packageSizeTopic, packageSizeDTO);
        try {
            kafkaTemplate.send(packageSizeTopic, packageSizeDTO.getId().toString(), packageSizeDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent package size with ID: {} to topic: {}",
                                    packageSizeDTO.getId(), packageSizeTopic);
                        } else {
                            log.error("Failed to send package size with ID: {} to topic: {}",
                                    packageSizeDTO.getId(), packageSizeTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending package size message to Kafka", e);
            throw new KafkaException("Failed to send package size message", e);
        }
    }
}

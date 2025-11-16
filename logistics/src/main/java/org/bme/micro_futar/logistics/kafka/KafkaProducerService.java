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

    @Value("${kafka.topics.shipment-route-courier-topic}")
    private String shipmentRouteCourierTopic;

    @Value("${kafka.topics.depo-topic}")
    private String depoTopic;

    @Value("${kafka.topics.shipment-route-topic}")
    private String shipmentRouteTopic;

    @Value("${kafka.topics.courier-topic}")
    private String courierTopic;

    @Value("${kafka.topics.vehicle-topic}")
    private String vehicleTopic;

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

    public void sendShipmentRouteCourier(ShipmentRouteCourierDTO shipmentRouteCourierDTO) {
        log.info("Sending shipment route courier message to topic {}: {}", shipmentRouteCourierTopic, shipmentRouteCourierDTO);
        try {
            kafkaTemplate.send(shipmentRouteCourierTopic, shipmentRouteCourierDTO.getId().toString(), shipmentRouteCourierDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent shipment route courier with ID: {} to topic: {}",
                                    shipmentRouteCourierDTO.getId(), shipmentRouteCourierTopic);
                        } else {
                            log.error("Failed to send shipment route courier with ID: {} to topic: {}",
                                    shipmentRouteCourierDTO.getId(), shipmentRouteCourierTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending shipment route courier message to Kafka", e);
            throw new KafkaException("Failed to send shipment route courier message", e);
        }
    }

    public void sendDepo(DepoDTO depoDTO) {
        log.info("Sending depo message to topic {}: {}", depoTopic, depoDTO);
        try {
            kafkaTemplate.send(depoTopic, depoDTO.getId().toString(), depoDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent depo with ID: {} to topic: {}",
                                    depoDTO.getId(), depoTopic);
                        } else {
                            log.error("Failed to send depo with ID: {} to topic: {}",
                                    depoDTO.getId(), depoTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending depo message to Kafka", e);
            throw new KafkaException("Failed to send depo message", e);
        }
    }

    public void sendShipmentRoute(ShipmentRouteDTO shipmentRouteDTO) {
        log.info("Sending shipment route message to topic {}: {}", shipmentRouteTopic, shipmentRouteDTO);
        try {
            kafkaTemplate.send(shipmentRouteTopic, shipmentRouteDTO.getId().toString(), shipmentRouteDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent shipment route with ID: {} to topic: {}",
                                    shipmentRouteDTO.getId(), shipmentRouteTopic);
                        } else {
                            log.error("Failed to send shipment route with ID: {} to topic: {}",
                                    shipmentRouteDTO.getId(), shipmentRouteTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending shipment route message to Kafka", e);
            throw new KafkaException("Failed to send shipment route message", e);
        }
    }

    public void sendCourier(CourierDTO courierDTO) {
        log.info("Sending courier message to topic {}: {}", courierTopic, courierDTO);
        try {
            kafkaTemplate.send(courierTopic, courierDTO.getId().toString(), courierDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent courier with ID: {} to topic: {}",
                                    courierDTO.getId(), courierTopic);
                        } else {
                            log.error("Failed to send courier with ID: {} to topic: {}",
                                    courierDTO.getId(), courierTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending courier message to Kafka", e);
            throw new KafkaException("Failed to send courier message", e);
        }
    }

    public void sendVehicle(VehicleDTO vehicleDTO) {
        log.info("Sending vehicle message to topic {}: {}", vehicleTopic, vehicleDTO);
        try {
            kafkaTemplate.send(vehicleTopic, vehicleDTO.getId().toString(), vehicleDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent vehicle with ID: {} to topic: {}",
                                    vehicleDTO.getId(), vehicleTopic);
                        } else {
                            log.error("Failed to send vehicle with ID: {} to topic: {}",
                                    vehicleDTO.getId(), vehicleTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending vehicle message to Kafka", e);
            throw new KafkaException("Failed to send vehicle message", e);
        }
    }
}

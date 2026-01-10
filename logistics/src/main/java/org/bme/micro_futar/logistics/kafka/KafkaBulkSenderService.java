package org.bme.micro_futar.logistics.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.mappers.*;
import org.bme.micro_futar.logistics.repositories.*;
import org.springframework.stereotype.Service;

/**
 * Service that queries all entities from the database and sends them to Kafka
 * using the existing KafkaProducerService methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaBulkSenderService {

    private final KafkaProducerService kafkaProducerService;

    // Repositories
    private final LocationRegionRepository locationRegionRepository;
    private final LocationCountryRepository locationCountryRepository;
    private final LocationCityRepository locationCityRepository;
    private final CountryPriceRepository countryPriceRepository;
    private final PackageSizeRepository packageSizeRepository;
    private final DepoRepository depoRepository;
    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentRouteRepository shipmentRouteRepository;
    private final ShipmentRouteCourierRepository shipmentRouteCourierRepository;

    // Mappers
    private final LocationRegionMapper locationRegionMapper;
    private final LocationCountryMapper locationCountryMapper;
    private final LocationCityMapper locationCityMapper;
    private final CountryPriceMapper countryPriceMapper;
    private final PackageSizeMapper packageSizeMapper;
    private final DepoMapper depoMapper;
    private final CourierMapper courierMapper;
    private final VehicleMapper vehicleMapper;
    private final ShipmentRouteMapper shipmentRouteMapper;
    private final ShipmentRouteCourierMapper shipmentRouteCourierMapper;

    public void sendAllEntitiesToKafka() {
        log.info("Starting bulk send of all entities to Kafka");

        sendLocationRegions();
        sendLocationCountries();
        sendLocationCities();
        sendCountryPrices();
        sendPackageSizes();
        sendDepos();
        sendCouriers();
        sendVehicles();
        sendShipmentRoutes();
        sendShipmentRouteCouriers();

        log.info("Completed bulk send of all entities to Kafka");
    }

    public void sendLocationRegions() {
        log.info("Sending all LocationRegion entities to Kafka");
        var entities = locationRegionRepository.findAll();
        entities.stream()
                .map(locationRegionMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendLocationRegion(dto);
                    } catch (Exception e) {
                        log.error("Failed to send LocationRegion with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} LocationRegion entities to Kafka", entities.size());
    }

    public void sendLocationCountries() {
        log.info("Sending all LocationCountry entities to Kafka");
        var entities = locationCountryRepository.findAll();
        entities.stream()
                .map(locationCountryMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendLocationCountry(dto);
                    } catch (Exception e) {
                        log.error("Failed to send LocationCountry with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} LocationCountry entities to Kafka", entities.size());
    }

    public void sendLocationCities() {
        log.info("Sending all LocationCity entities to Kafka");
        var entities = locationCityRepository.findAll();
        entities.stream()
                .map(locationCityMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendLocationCity(dto);
                    } catch (Exception e) {
                        log.error("Failed to send LocationCity with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} LocationCity entities to Kafka", entities.size());
    }

    public void sendCountryPrices() {
        log.info("Sending all CountryPrice entities to Kafka");
        var entities = countryPriceRepository.findAll();
        entities.stream()
                .map(countryPriceMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendCountryPrice(dto);
                    } catch (Exception e) {
                        log.error("Failed to send CountryPrice with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} CountryPrice entities to Kafka", entities.size());
    }

    public void sendPackageSizes() {
        log.info("Sending all PackageSize entities to Kafka");
        var entities = packageSizeRepository.findAll();
        entities.stream()
                .map(packageSizeMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendPackageSize(dto);
                    } catch (Exception e) {
                        log.error("Failed to send PackageSize with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} PackageSize entities to Kafka", entities.size());
    }

    public void sendDepos() {
        log.info("Sending all Depo entities to Kafka");
        var entities = depoRepository.findAll();
        entities.stream()
                .map(depoMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendDepo(dto);
                    } catch (Exception e) {
                        log.error("Failed to send Depo with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} Depo entities to Kafka", entities.size());
    }

    public void sendCouriers() {
        log.info("Sending all Courier entities to Kafka");
        var entities = courierRepository.findAll();
        entities.stream()
                .map(courierMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendCourier(dto);
                    } catch (Exception e) {
                        log.error("Failed to send Courier with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} Courier entities to Kafka", entities.size());
    }

    public void sendVehicles() {
        log.info("Sending all Vehicle entities to Kafka");
        var entities = vehicleRepository.findAll();
        entities.stream()
                .map(vehicleMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendVehicle(dto);
                    } catch (Exception e) {
                        log.error("Failed to send Vehicle with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} Vehicle entities to Kafka", entities.size());
    }

    public void sendShipmentRoutes() {
        log.info("Sending all ShipmentRoute entities to Kafka");
        var entities = shipmentRouteRepository.findAll();
        entities.stream()
                .map(shipmentRouteMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendShipmentRoute(dto);
                    } catch (Exception e) {
                        log.error("Failed to send ShipmentRoute with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} ShipmentRoute entities to Kafka", entities.size());
    }

    public void sendShipmentRouteCouriers() {
        log.info("Sending all ShipmentRouteCourier entities to Kafka");
        var entities = shipmentRouteCourierRepository.findAll();
        entities.stream()
                .map(shipmentRouteCourierMapper::toDTO)
                .forEach(dto -> {
                    try {
                        kafkaProducerService.sendShipmentRouteCourier(dto);
                    } catch (Exception e) {
                        log.error("Failed to send ShipmentRouteCourier with ID: {}", dto.getId(), e);
                    }
                });
        log.info("Sent {} ShipmentRouteCourier entities to Kafka", entities.size());
    }

    public void sendEntitiesByType(String entityType) {
        log.info("Sending entities of type: {}", entityType);

        switch (entityType.toLowerCase()) {
            case "locationregion" -> sendLocationRegions();
            case "locationcountry" -> sendLocationCountries();
            case "locationcity" -> sendLocationCities();
            case "countryprice" -> sendCountryPrices();
            case "packagesize" -> sendPackageSizes();
            case "depo" -> sendDepos();
            case "courier" -> sendCouriers();
            case "vehicle" -> sendVehicles();
            case "shipmentroute" -> sendShipmentRoutes();
            case "shipmentroutecourier" -> sendShipmentRouteCouriers();
            default -> {
                log.warn("Unknown entity type: {}", entityType);
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
            }
        }
    }
}


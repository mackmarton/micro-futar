package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.kafka.KafkaBulkSenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for bulk sending entities to Kafka.
 * Provides endpoints to send all entities or specific entity types.
 */
@Slf4j
@RestController
@RequestMapping("/api/kafka/bulk")
@RequiredArgsConstructor
public class KafkaBulkSenderController {

    private final KafkaBulkSenderService kafkaBulkSenderService;

    /**
     * Send all entities to their respective Kafka topics.
     */
    @PostMapping("/send-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendAllToKafka() {
        log.info("Received request to send all entities to Kafka");
        try {
            kafkaBulkSenderService.sendAllEntitiesToKafka();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending all entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send specific entity type to Kafka.
     *
     * @param entityType The entity type (e.g., "locationregion", "depo", "courier")
     */
    @PostMapping("/send/{entityType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendByType(@PathVariable String entityType) {
        log.info("Received request to send {} entities to Kafka", entityType);
        try {
            kafkaBulkSenderService.sendEntitiesByType(entityType);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", entityType + " entities sent to Kafka successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error sending {} entities to Kafka", entityType, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send " + entityType + " entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send LocationRegion entities to Kafka.
     */
    @PostMapping("/send-location-regions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendLocationRegions() {
        log.info("Received request to send LocationRegion entities to Kafka");
        try {
            kafkaBulkSenderService.sendLocationRegions();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "LocationRegion entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending LocationRegion entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send LocationRegion entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send LocationCountry entities to Kafka.
     */
    @PostMapping("/send-location-countries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendLocationCountries() {
        log.info("Received request to send LocationCountry entities to Kafka");
        try {
            kafkaBulkSenderService.sendLocationCountries();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "LocationCountry entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending LocationCountry entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send LocationCountry entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send LocationCity entities to Kafka.
     */
    @PostMapping("/send-location-cities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendLocationCities() {
        log.info("Received request to send LocationCity entities to Kafka");
        try {
            kafkaBulkSenderService.sendLocationCities();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "LocationCity entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending LocationCity entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send LocationCity entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send CountryPrice entities to Kafka.
     */
    @PostMapping("/send-country-prices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendCountryPrices() {
        log.info("Received request to send CountryPrice entities to Kafka");
        try {
            kafkaBulkSenderService.sendCountryPrices();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "CountryPrice entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending CountryPrice entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send CountryPrice entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send PackageSize entities to Kafka.
     */
    @PostMapping("/send-package-sizes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendPackageSizes() {
        log.info("Received request to send PackageSize entities to Kafka");
        try {
            kafkaBulkSenderService.sendPackageSizes();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "PackageSize entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending PackageSize entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send PackageSize entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send Depo entities to Kafka.
     */
    @PostMapping("/send-depos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendDepos() {
        log.info("Received request to send Depo entities to Kafka");
        try {
            kafkaBulkSenderService.sendDepos();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Depo entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending Depo entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send Depo entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send Courier entities to Kafka.
     */
    @PostMapping("/send-couriers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendCouriers() {
        log.info("Received request to send Courier entities to Kafka");
        try {
            kafkaBulkSenderService.sendCouriers();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Courier entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending Courier entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send Courier entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send Vehicle entities to Kafka.
     */
    @PostMapping("/send-vehicles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendVehicles() {
        log.info("Received request to send Vehicle entities to Kafka");
        try {
            kafkaBulkSenderService.sendVehicles();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Vehicle entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending Vehicle entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send Vehicle entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send Shipment entities to Kafka.
     */
    @PostMapping("/send-shipments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendShipments() {
        log.info("Received request to send Shipment entities to Kafka");
        try {
            kafkaBulkSenderService.sendShipments();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Shipment entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending Shipment entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send Shipment entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send ShipmentRoute entities to Kafka.
     */
    @PostMapping("/send-shipment-routes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendShipmentRoutes() {
        log.info("Received request to send ShipmentRoute entities to Kafka");
        try {
            kafkaBulkSenderService.sendShipmentRoutes();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "ShipmentRoute entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending ShipmentRoute entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send ShipmentRoute entities to Kafka: " + e.getMessage()
            ));
        }
    }

    /**
     * Send ShipmentRouteCourier entities to Kafka.
     */
    @PostMapping("/send-shipment-route-couriers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendShipmentRouteCouriers() {
        log.info("Received request to send ShipmentRouteCourier entities to Kafka");
        try {
            kafkaBulkSenderService.sendShipmentRouteCouriers();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "ShipmentRouteCourier entities sent to Kafka successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending ShipmentRouteCourier entities to Kafka", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send ShipmentRouteCourier entities to Kafka: " + e.getMessage()
            ));
        }
    }
}


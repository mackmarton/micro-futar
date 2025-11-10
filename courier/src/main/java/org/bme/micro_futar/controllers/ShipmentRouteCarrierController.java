package org.bme.micro_futar.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.ShipmentRouteCarrierService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCarrierDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/shipment-route-carriers")
@RequiredArgsConstructor
public class ShipmentRouteCarrierController {

    private final ShipmentRouteCarrierService shipmentRouteCarrierService;

    @GetMapping
    public ResponseEntity<List<ShipmentRouteCarrierDTO>> findAll() {
        log.debug("Finding all shipmentRouteCarriers");
        List<ShipmentRouteCarrierDTO> shipmentRouteCarriers = shipmentRouteCarrierService.findAll();
        return ResponseEntity.ok(shipmentRouteCarriers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentRouteCarrierDTO> findById(@PathVariable Long id) {
        log.debug("Finding shipmentRouteCarrier by id: {}", id);
        return shipmentRouteCarrierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.ShipmentRouteService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/shipment-routes")
@RequiredArgsConstructor
public class ShipmentRouteController {

    private final ShipmentRouteService shipmentRouteService;

    @GetMapping
    public ResponseEntity<List<ShipmentRouteDTO>> findAll() {
        log.debug("Finding all shipmentRoutes");
        List<ShipmentRouteDTO> shipmentRoutes = shipmentRouteService.findAll();
        return ResponseEntity.ok(shipmentRoutes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentRouteDTO> findById(@PathVariable Long id) {
        log.debug("Finding shipmentRoute by id: {}", id);
        return shipmentRouteService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


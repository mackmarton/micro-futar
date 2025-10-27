package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.logistics.services.ShipmentRouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipment-routes")
@RequiredArgsConstructor
public class ShipmentRouteController {

    private final ShipmentRouteService shipmentRouteService;

    @GetMapping
    public ResponseEntity<List<ShipmentRouteDTO>> getAllShipmentRoutes() {
        List<ShipmentRouteDTO> shipmentRoutes = shipmentRouteService.getAllShipmentRoutes();
        return ResponseEntity.ok(shipmentRoutes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentRouteDTO> getShipmentRouteById(@PathVariable Long id) {
        return shipmentRouteService.getShipmentRouteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

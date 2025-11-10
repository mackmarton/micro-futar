package org.bme.micro_futar.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> findAll() {
        log.debug("Finding all shipments");
        List<ShipmentDTO> shipments = shipmentService.findAll();
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDTO> findById(@PathVariable Long id) {
        log.debug("Finding shipment by id: {}", id);
        return shipmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


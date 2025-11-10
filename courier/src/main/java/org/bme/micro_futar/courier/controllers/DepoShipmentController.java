package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.DepoShipmentService;
import org.bme.micro_futar.shared.dtos.DepoShipmentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/depo-shipments")
@RequiredArgsConstructor
public class DepoShipmentController {

    private final DepoShipmentService depoShipmentService;

    @GetMapping
    public ResponseEntity<List<DepoShipmentDTO>> findAll() {
        log.debug("Finding all depoShipments");
        List<DepoShipmentDTO> depoShipments = depoShipmentService.findAll();
        return ResponseEntity.ok(depoShipments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepoShipmentDTO> findById(@PathVariable Long id) {
        log.debug("Finding depoShipment by id: {}", id);
        return depoShipmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


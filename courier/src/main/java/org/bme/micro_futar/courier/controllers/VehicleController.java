package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.VehicleService;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleDTO>> findAll() {
        log.debug("Finding all vehicles");
        List<VehicleDTO> vehicles = vehicleService.findAll();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> findById(@PathVariable Long id) {
        log.debug("Finding vehicle by id: {}", id);
        return vehicleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}



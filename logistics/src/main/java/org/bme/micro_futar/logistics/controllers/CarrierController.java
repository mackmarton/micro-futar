package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.CarrierDTO;
import org.bme.micro_futar.logistics.services.CarrierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
@RequiredArgsConstructor
public class CarrierController {

    private final CarrierService carrierService;

    @GetMapping
    public ResponseEntity<List<CarrierDTO>> getAllCarriers() {
        List<CarrierDTO> carriers = carrierService.getAllCarriers();
        return ResponseEntity.ok(carriers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarrierDTO> getCarrierById(@PathVariable Long id) {
        return carrierService.getCarrierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CarrierDTO> createCarrier(@RequestBody CarrierDTO carrierDTO) {
        CarrierDTO createdCarrier = carrierService.createCarrier(carrierDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCarrier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarrierDTO> updateCarrier(@PathVariable Long id, @RequestBody CarrierDTO carrierDTO) {
        return carrierService.updateCarrier(id, carrierDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarrier(@PathVariable Long id) {
        boolean deleted = carrierService.deleteCarrier(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

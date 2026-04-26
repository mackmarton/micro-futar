package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.services.CourierService;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @GetMapping
    public ResponseEntity<List<CourierDTO>> getAllCouriers() {
        List<CourierDTO> couriers = courierService.getAllCouriers();
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourierDTO> getCourierById(@PathVariable Long id) {
        return courierService.getCourierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("by-depo/{depoId}")
    public ResponseEntity<List<CourierDTO>> getCourierByDepoId(@PathVariable Long depoId) {
        return ResponseEntity.ok(courierService.getCouriersByDepoId(depoId));
    }

    @GetMapping("cross-depo")
    public ResponseEntity<List<CourierDTO>> getCrossDepoCouriers() {
        return ResponseEntity.ok(courierService.getCrossDepoCouriers());
    }

    @PostMapping
    public ResponseEntity<CourierDTO> createCourier(@RequestBody CourierDTO courierDTO) {
        CourierDTO createdCourier = courierService.createCourier(courierDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourierDTO> updateCourier(@PathVariable Long id, @RequestBody CourierDTO courierDTO) {
        return courierService.updateCourier(id, courierDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourier(@PathVariable Long id) {
        boolean deleted = courierService.deleteCourier(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

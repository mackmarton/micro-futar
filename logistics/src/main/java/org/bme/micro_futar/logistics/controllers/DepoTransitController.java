package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.services.DepoTransitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depo-transits")
@RequiredArgsConstructor
public class DepoTransitController {

    private final DepoTransitService depoTransitService;

    @GetMapping
    public ResponseEntity<List<DepoTransitDTO>> getAllDepoTransits() {
        List<DepoTransitDTO> depoTransits = depoTransitService.getAllDepoTransits();
        return ResponseEntity.ok(depoTransits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepoTransitDTO> getDepoTransitById(@PathVariable Long id) {
        return depoTransitService.getDepoTransitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("by-origin/{originDepoId}")
    public ResponseEntity<List<DepoTransitDTO>> getDepoTransitsByOriginDepoId(@PathVariable Long originDepoId) {
        List<DepoTransitDTO> depoTransits = depoTransitService.getDepoTransitsByOriginDepoId(originDepoId);
        return ResponseEntity.ok(depoTransits);
    }

    @GetMapping("by-destination/{destinationDepoId}")
    public ResponseEntity<List<DepoTransitDTO>> getDepoTransitsByDestinationDepoId(@PathVariable Long destinationDepoId) {
        List<DepoTransitDTO> depoTransits = depoTransitService.getDepoTransitsByDestinationDepoId(destinationDepoId);
        return ResponseEntity.ok(depoTransits);
    }

    @PostMapping
    public ResponseEntity<DepoTransitDTO> createDepoTransit(@RequestBody DepoTransitDTO depoTransitDTO) {
        DepoTransitDTO createdDepoTransit = depoTransitService.createDepoTransit(depoTransitDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepoTransit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepoTransitDTO> updateDepoTransit(@PathVariable Long id, @RequestBody DepoTransitDTO depoTransitDTO) {
        return depoTransitService.updateDepoTransit(id, depoTransitDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepoTransit(@PathVariable Long id) {
        boolean deleted = depoTransitService.deleteDepoTransit(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

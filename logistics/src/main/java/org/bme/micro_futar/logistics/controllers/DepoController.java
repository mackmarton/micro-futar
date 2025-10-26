package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.DepoDTO;
import org.bme.micro_futar.logistics.services.DepoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depos")
@RequiredArgsConstructor
public class DepoController {

    private final DepoService depoService;

    @GetMapping
    public ResponseEntity<List<DepoDTO>> getAllDepos() {
        List<DepoDTO> depos = depoService.getAllDepos();
        return ResponseEntity.ok(depos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepoDTO> getDepoById(@PathVariable Long id) {
        return depoService.getDepoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DepoDTO> createDepo(@RequestBody DepoDTO depoDTO) {
        DepoDTO createdDepo = depoService.createDepo(depoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepoDTO> updateDepo(@PathVariable Long id, @RequestBody DepoDTO depoDTO) {
        return depoService.updateDepo(id, depoDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepo(@PathVariable Long id) {
        boolean deleted = depoService.deleteDepo(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}


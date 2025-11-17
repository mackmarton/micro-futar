package org.bme.micro_futar.tracking.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.tracking.services.DepoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/depos")
@RequiredArgsConstructor
public class DepoController {

    private final DepoService depoService;

    @GetMapping
    public ResponseEntity<List<DepoDTO>> findAll() {
        log.debug("Finding all depos");
        List<DepoDTO> depos = depoService.findAll();
        return ResponseEntity.ok(depos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepoDTO> findById(@PathVariable Long id) {
        log.debug("Finding depo by id: {}", id);
        return depoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


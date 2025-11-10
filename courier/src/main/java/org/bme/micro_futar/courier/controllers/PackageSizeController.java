package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.PackageSizeService;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/package-sizes")
@RequiredArgsConstructor
public class PackageSizeController {

    private final PackageSizeService packageSizeService;

    @GetMapping
    public ResponseEntity<List<PackageSizeDTO>> findAll() {
        log.debug("Finding all packageSizes");
        List<PackageSizeDTO> packageSizes = packageSizeService.findAll();
        return ResponseEntity.ok(packageSizes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageSizeDTO> findById(@PathVariable Long id) {
        log.debug("Finding packageSize by id: {}", id);
        return packageSizeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


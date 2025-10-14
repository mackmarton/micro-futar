package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.PackageSizeService;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/package-sizes")
public class PackageSizeController {

    private final PackageSizeService packageSizeService;

    @GetMapping
    public ResponseEntity<List<PackageSizeDTO>> getAllPackageSizes() {
        List<PackageSizeDTO> packageSizes = packageSizeService.getAllPackageSizes();
        return ResponseEntity.ok(packageSizes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageSizeDTO> getPackageSizeById(@PathVariable Long id) {
        Optional<PackageSizeDTO> packageSize = packageSizeService.getPackageSizeById(id);
        return packageSize.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

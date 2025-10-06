package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.services.PackageSizeService;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/package-sizes")
public class PackageSizeController {

    @Autowired
    private PackageSizeService packageSizeService;

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

    @PostMapping
    public ResponseEntity<PackageSizeDTO> createPackageSize(@RequestBody PackageSizeDTO packageSizeDTO) {
        PackageSizeDTO createdPackageSize = packageSizeService.createPackageSize(packageSizeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPackageSize);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageSizeDTO> updatePackageSize(@PathVariable Long id, @RequestBody PackageSizeDTO packageSizeDTO) {
        Optional<PackageSizeDTO> updatedPackageSize = packageSizeService.updatePackageSize(id, packageSizeDTO);
        return updatedPackageSize.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackageSize(@PathVariable Long id) {
        boolean deleted = packageSizeService.deletePackageSize(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

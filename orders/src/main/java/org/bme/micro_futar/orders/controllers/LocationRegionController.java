package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.LocationRegionService;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regions")
public class LocationRegionController {

    private final LocationRegionService locationRegionService;

    @GetMapping
    public ResponseEntity<List<LocationRegionDTO>> getAllRegions() {
        List<LocationRegionDTO> regions = locationRegionService.getAllRegions();
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationRegionDTO> getRegionById(@PathVariable Long id) {
        Optional<LocationRegionDTO> region = locationRegionService.getRegionById(id);
        return region.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

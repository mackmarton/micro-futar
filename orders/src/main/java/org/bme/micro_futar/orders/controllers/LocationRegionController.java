package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.services.LocationRegionService;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/regions")
public class LocationRegionController {

    @Autowired
    private LocationRegionService locationRegionService;

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

    //ADMIN
    @PostMapping
    public ResponseEntity<LocationRegionDTO> createRegion(@RequestBody LocationRegionDTO locationRegionDTO) {
        LocationRegionDTO createdRegion = locationRegionService.createRegion(locationRegionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRegion);
    }

    //ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<LocationRegionDTO> updateRegion(@PathVariable Long id, @RequestBody LocationRegionDTO locationRegionDTO) {
        Optional<LocationRegionDTO> updatedRegion = locationRegionService.updateRegion(id, locationRegionDTO);
        return updatedRegion.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
        boolean deleted = locationRegionService.deleteRegion(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

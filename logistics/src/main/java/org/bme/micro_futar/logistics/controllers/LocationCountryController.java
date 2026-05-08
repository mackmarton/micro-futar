package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.services.LocationCountryService;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/countries")
public class LocationCountryController {

    private final LocationCountryService locationCountryService;

    @GetMapping
    public ResponseEntity<List<LocationCountryDTO>> getAllCountries(@RequestParam(required = false) Long regionId) {
        if (regionId != null) {
            List<LocationCountryDTO> countries = locationCountryService.getAllCountriesByRegionId(regionId);
        }
        List<LocationCountryDTO> countries = locationCountryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationCountryDTO> getCountryById(@PathVariable Long id) {
        Optional<LocationCountryDTO> country = locationCountryService.getCountryById(id);
        return country.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LocationCountryDTO> createCountry(@RequestBody LocationCountryDTO locationCountryDTO) {
        LocationCountryDTO createdCountry = locationCountryService.createCountry(locationCountryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCountry);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationCountryDTO> updateCountry(@PathVariable Long id, @RequestBody LocationCountryDTO locationCountryDTO) {
        Optional<LocationCountryDTO> updatedCountry = locationCountryService.updateCountry(id, locationCountryDTO);
        return updatedCountry.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Long id) {
        boolean deleted = locationCountryService.deleteCountry(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

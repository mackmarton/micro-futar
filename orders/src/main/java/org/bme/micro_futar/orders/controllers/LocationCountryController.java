package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.LocationCountryService;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/countries")
public class LocationCountryController {

    private final LocationCountryService locationCountryService;

    @GetMapping
    public ResponseEntity<List<LocationCountryDTO>> getAllCountries() {
        List<LocationCountryDTO> countries = locationCountryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationCountryDTO> getCountryById(@PathVariable Long id) {
        Optional<LocationCountryDTO> country = locationCountryService.getCountryById(id);
        return country.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

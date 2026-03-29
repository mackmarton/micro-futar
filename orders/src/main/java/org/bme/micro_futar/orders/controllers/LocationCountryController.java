package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.LocationCityService;
import org.bme.micro_futar.orders.services.LocationCountryService;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/countries")
public class LocationCountryController {

    private final LocationCountryService locationCountryService;
    private final LocationCityService locationCityService;

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

    @GetMapping("/{id}/cities")
    public ResponseEntity<List<LocationCityDTO>> getAllCitiesByCountryId(@PathVariable Long id) {
        List<LocationCityDTO> cities = locationCityService.getAllCitiesByCountryId(id);
        return ResponseEntity.ok(cities);
    }
}

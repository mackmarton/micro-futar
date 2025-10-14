package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.LocationCityService;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cities")
public class LocationCityController {

    private final LocationCityService locationCityService;

    @GetMapping
    public ResponseEntity<List<LocationCityDTO>> getAllCities() {
        List<LocationCityDTO> cities = locationCityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationCityDTO> getCityById(@PathVariable Long id) {
        Optional<LocationCityDTO> city = locationCityService.getCityById(id);
        return city.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

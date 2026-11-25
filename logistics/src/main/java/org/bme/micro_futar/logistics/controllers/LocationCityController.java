package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.GeocodeJobStatusDTO;
import org.bme.micro_futar.logistics.services.LocationCityService;
import org.bme.micro_futar.logistics.services.geocoding.CityGeocodingJob;
import org.bme.micro_futar.logistics.services.geocoding.CityGeocodingService;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cities")
public class LocationCityController {

    private final LocationCityService locationCityService;
    private final CityGeocodingService cityGeocodingService;
    private final CityGeocodingJob cityGeocodingJob;

    @GetMapping
    public ResponseEntity<List<LocationCityDTO>> getAllCities(@RequestParam(required = false) Long countryId) {
        if (countryId != null) {
            List<LocationCityDTO> cities = locationCityService.getAllCitiesByCountryId(countryId);
            return ResponseEntity.ok(cities);
        }
        List<LocationCityDTO> cities = locationCityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationCityDTO> getCityById(@PathVariable Long id) {
        Optional<LocationCityDTO> city = locationCityService.getCityById(id);
        return city.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LocationCityDTO> createCity(@RequestBody LocationCityDTO locationCityDTO) {
        LocationCityDTO createdCity = cityGeocodingService.createCity(locationCityDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationCityDTO> updateCity(@PathVariable Long id, @RequestBody LocationCityDTO locationCityDTO) {
        Optional<LocationCityDTO> updatedCity = cityGeocodingService.updateCity(id, locationCityDTO);
        return updatedCity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        boolean deleted = locationCityService.deleteCity(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /** Starts the background geocoding of all cities without geo data; poll {@code /geocode-missing/status}. */
    @PostMapping("/geocode-missing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GeocodeJobStatusDTO> geocodeMissing() {
        return ResponseEntity.accepted().body(cityGeocodingJob.start());
    }

    @GetMapping("/geocode-missing/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GeocodeJobStatusDTO> geocodeMissingStatus() {
        return ResponseEntity.ok(cityGeocodingJob.getStatus());
    }

    /** Re-geocodes one city, even a manual one (it becomes non-manual). */
    @PostMapping("/{id}/geocode")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationCityDTO> geocodeCity(@PathVariable Long id) {
        return cityGeocodingService.regeocode(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

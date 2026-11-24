package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.CountryBaseFeeDTO;
import org.bme.micro_futar.logistics.services.CountryBaseFeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/country-base-fees")
@RequiredArgsConstructor
public class CountryBaseFeeController {

    private final CountryBaseFeeService countryBaseFeeService;

    @GetMapping
    public ResponseEntity<List<CountryBaseFeeDTO>> getAllCountryBaseFees() {
        return ResponseEntity.ok(countryBaseFeeService.getAllCountryBaseFees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryBaseFeeDTO> getCountryBaseFeeById(@PathVariable Long id) {
        return countryBaseFeeService.getCountryBaseFeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CountryBaseFeeDTO> createCountryBaseFee(@RequestBody CountryBaseFeeDTO countryBaseFeeDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(countryBaseFeeService.createCountryBaseFee(countryBaseFeeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CountryBaseFeeDTO> updateCountryBaseFee(@PathVariable Long id, @RequestBody CountryBaseFeeDTO countryBaseFeeDTO) {
        return countryBaseFeeService.updateCountryBaseFee(id, countryBaseFeeDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountryBaseFee(@PathVariable Long id) {
        return countryBaseFeeService.deleteCountryBaseFee(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

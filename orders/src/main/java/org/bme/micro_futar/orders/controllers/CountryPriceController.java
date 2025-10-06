package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.services.CountryPriceService;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/country-prices")
public class CountryPriceController {

    @Autowired
    private CountryPriceService countryPriceService;

    @GetMapping
    public ResponseEntity<List<CountryPriceDTO>> getAllCountryPrices() {
        List<CountryPriceDTO> countryPrices = countryPriceService.getAllCountryPrices();
        return ResponseEntity.ok(countryPrices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryPriceDTO> getCountryPriceById(@PathVariable Long id) {
        Optional<CountryPriceDTO> countryPrice = countryPriceService.getCountryPriceById(id);
        return countryPrice.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CountryPriceDTO> createCountryPrice(@RequestBody CountryPriceDTO countryPriceDTO) {
        CountryPriceDTO createdCountryPrice = countryPriceService.createCountryPrice(countryPriceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCountryPrice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CountryPriceDTO> updateCountryPrice(@PathVariable Long id, @RequestBody CountryPriceDTO countryPriceDTO) {
        Optional<CountryPriceDTO> updatedCountryPrice = countryPriceService.updateCountryPrice(id, countryPriceDTO);
        return updatedCountryPrice.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountryPrice(@PathVariable Long id) {
        boolean deleted = countryPriceService.deleteCountryPrice(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

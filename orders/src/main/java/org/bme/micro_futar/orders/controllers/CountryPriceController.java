package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.CountryPriceService;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/country-prices")
public class CountryPriceController {

    private final CountryPriceService countryPriceService;

    @GetMapping
    public ResponseEntity<List<CountryPriceDTO>> getAllCountryPrices(
            @RequestParam(required = false) Long originCountryId,
            @RequestParam(required = false) Long destinationCountryId) {
        if (originCountryId != null && destinationCountryId != null) {
            List<CountryPriceDTO> countryPricesByCountries = countryPriceService.getAllCountryPricesByCountries(originCountryId, destinationCountryId);
            return ResponseEntity.ok(countryPricesByCountries);
        }
        List<CountryPriceDTO> countryPrices = countryPriceService.getAllCountryPrices();
        return ResponseEntity.ok(countryPrices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryPriceDTO> getCountryPriceById(@PathVariable Long id) {
        Optional<CountryPriceDTO> countryPrice = countryPriceService.getCountryPriceById(id);
        return countryPrice.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

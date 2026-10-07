package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.services.CurrencyService;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<CurrencyDTO>> getAllCurrencies() {
        List<CurrencyDTO> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyDTO> getCurrencyById(@PathVariable Long id) {
        Optional<CurrencyDTO> currency = currencyService.getCurrencyById(id);
        return currency.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CurrencyDTO> createCurrency(@RequestBody CurrencyDTO currencyDTO) {
        CurrencyDTO createdCurrency = currencyService.createCurrency(currencyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCurrency);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CurrencyDTO> updateCurrency(@PathVariable Long id, @RequestBody CurrencyDTO currencyDTO) {
        Optional<CurrencyDTO> updatedCurrency = currencyService.updateCurrency(id, currencyDTO);
        return updatedCurrency.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable Long id) {
        boolean deleted = currencyService.deleteCurrency(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

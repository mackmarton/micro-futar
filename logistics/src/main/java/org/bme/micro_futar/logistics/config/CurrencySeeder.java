package org.bme.micro_futar.logistics.config;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.Currency;
import org.bme.micro_futar.logistics.repositories.CurrencyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrencySeeder implements CommandLineRunner {

    private final CurrencyRepository currencyRepository;

    @Override
    public void run(String... args) {
        if (!currencyRepository.existsByCode("HUF")) {
            Currency huf = new Currency();
            huf.setCode("HUF");
            huf.setName("Hungarian Forint");
            huf.setSymbol("Ft");
            currencyRepository.save(huf);
        }
    }
}

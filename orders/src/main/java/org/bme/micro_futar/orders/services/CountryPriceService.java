package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.entities.CountryPrice;
import org.bme.micro_futar.orders.repositories.CountryPriceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CountryPriceService {

    private final CountryPriceRepository countryPriceRepository;

    public Optional<CountryPrice> findPriceByCountriesAndSize(long originCountryId, long destinationCountryId, long packageSizeId) {
        return countryPriceRepository.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(originCountryId, destinationCountryId, packageSizeId);
    }

    public CountryPrice save(CountryPrice countryPrice) {
        return countryPriceRepository.save(countryPrice);
    }

    public Optional<CountryPrice> findById(Long id) {
        return countryPriceRepository.findById(id);
    }

    public void deleteById(Long id) {
        countryPriceRepository.deleteById(id);
    }
}

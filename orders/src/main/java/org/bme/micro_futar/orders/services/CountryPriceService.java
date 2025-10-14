package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.entities.CountryPrice;
import org.bme.micro_futar.orders.mappers.CountryPriceMapper;
import org.bme.micro_futar.orders.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryPriceService {

    private final CountryPriceRepository countryPriceRepository;
    private final CountryPriceMapper countryPriceMapper;

    public List<CountryPriceDTO> getAllCountryPrices() {
        return countryPriceRepository.findAll().stream()
                .map(countryPriceMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CountryPriceDTO> getCountryPriceById(Long id) {
        return countryPriceRepository.findById(id)
                .map(countryPriceMapper::toDTO);
    }

    public Optional<CountryPrice> findPriceByCountriesAndSize(long originCountryId, long destinationCountryId, long packageSizeId) {
        return countryPriceRepository.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(originCountryId, destinationCountryId, packageSizeId);
    }
}

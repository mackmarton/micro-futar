package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.entities.CountryPrice;
import org.bme.micro_futar.orders.mappers.CountryPriceMapper;
import org.bme.micro_futar.orders.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CountryPriceService {

    private final CountryPriceRepository countryPriceRepository;
    private final CountryPriceMapper countryPriceMapper;

    public List<CountryPriceDTO> getAllCountryPrices() {
        return countryPriceRepository.findAll().stream()
                .map(countryPriceMapper::toDTO)
                .toList();
    }

    public Optional<CountryPriceDTO> getCountryPriceById(Long id) {
        return countryPriceRepository.findById(id)
                .map(countryPriceMapper::toDTO);
    }

    public Optional<CountryPriceDTO> findPriceByCountriesAndSize(long originCountryId, long destinationCountryId, long packageSizeId) {
        return countryPriceRepository
                .findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(originCountryId, destinationCountryId, packageSizeId)
                .map(countryPriceMapper::toDTO);
    }

    @Transactional
    public CountryPriceDTO saveCountryPrice(CountryPriceDTO countryPriceDTO) {
        CountryPrice countryPrice;

        // Check if entity already exists to avoid optimistic locking issues
        if (countryPriceDTO.getId() != null) {
            Optional<CountryPrice> existing = countryPriceRepository.findById(countryPriceDTO.getId());
            if (existing.isPresent()) {
                countryPrice = existing.get();
                // Update existing entity fields
                countryPrice.setOriginCountryId(countryPriceDTO.getOriginCountryId());
                countryPrice.setDestinationCountryId(countryPriceDTO.getDestinationCountryId());
                countryPrice.setPackageSizeId(countryPriceDTO.getPackageSizeId());
                countryPrice.setMinPrice(countryPriceDTO.getMinPrice());
                countryPrice.setMaxPrice(countryPriceDTO.getMaxPrice());
            } else {
                countryPrice = countryPriceMapper.toEntity(countryPriceDTO);
            }
        } else {
            countryPrice = countryPriceMapper.toEntity(countryPriceDTO);
        }

        return countryPriceMapper.toDTO(countryPriceRepository.save(countryPrice));
    }
}

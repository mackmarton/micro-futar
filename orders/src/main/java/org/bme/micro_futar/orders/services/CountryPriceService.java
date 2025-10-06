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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryPriceService {

    private CountryPriceRepository countryPriceRepository;
    private CountryPriceMapper countryPriceMapper;

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

    @Transactional
    public CountryPriceDTO createCountryPrice(CountryPriceDTO countryPriceDTO) {
        CountryPrice countryPrice = countryPriceMapper.toEntity(countryPriceDTO);
        CountryPrice savedCountryPrice = countryPriceRepository.save(countryPrice);
        return countryPriceMapper.toDTO(savedCountryPrice);
    }

    @Transactional
    public Optional<CountryPriceDTO> updateCountryPrice(Long id, CountryPriceDTO countryPriceDTO) {
        if (countryPriceDTO.getId() != null && !countryPriceDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return countryPriceRepository.findById(id)
                .map(existingCountryPrice -> {
                    CountryPrice updatedCountryPrice = countryPriceMapper.toEntity(countryPriceDTO);
                    CountryPrice savedCountryPrice = countryPriceRepository.save(updatedCountryPrice);
                    return countryPriceMapper.toDTO(savedCountryPrice);
                });
    }

    @Transactional
    public boolean deleteCountryPrice(Long id) {
        if (countryPriceRepository.existsById(id)) {
            countryPriceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

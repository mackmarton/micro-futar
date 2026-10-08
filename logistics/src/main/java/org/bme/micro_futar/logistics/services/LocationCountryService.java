package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.LocationCountryMapper;
import org.bme.micro_futar.logistics.repositories.CurrencyRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationCountryService {

    private static final String DEFAULT_CURRENCY_CODE = "HUF";

    private final LocationCountryRepository locationCountryRepository;
    private final LocationCountryMapper locationCountryMapper;
    private final KafkaProducerService kafkaProducerService;
    private final CurrencyRepository currencyRepository;

    public List<LocationCountryDTO> getAllCountries() {
        return locationCountryRepository.findAll().stream()
                .map(locationCountryMapper::toDTO)
                .toList();
    }

    public Optional<LocationCountryDTO> getCountryById(Long id) {
        return locationCountryRepository.findById(id)
                .map(locationCountryMapper::toDTO);
    }

    public List<LocationCountryDTO> getAllCountriesByRegionId(Long regionId) {
        return locationCountryRepository.findAllByRegionId(regionId).stream()
                .map(locationCountryMapper::toDTO)
                .toList();
    }

    @Transactional
    public LocationCountryDTO createCountry(LocationCountryDTO locationCountryDTO) {
        locationCountryDTO.setCurrencyCode(resolveCurrencyCode(locationCountryDTO.getCurrencyCode()));
        LocationCountry locationCountry = locationCountryMapper.toEntity(locationCountryDTO);
        locationCountry.setDeleted(false);
        LocationCountry savedCountry = locationCountryRepository.save(locationCountry);
        LocationCountryDTO result = locationCountryMapper.toDTO(savedCountry);
        kafkaProducerService.sendLocationCountry(result);
        return result;
    }

    @Transactional
    public Optional<LocationCountryDTO> updateCountry(Long id, LocationCountryDTO locationCountryDTO) {
        if (locationCountryDTO.getId() != null && !locationCountryDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }
        locationCountryDTO.setCurrencyCode(resolveCurrencyCode(locationCountryDTO.getCurrencyCode()));

        return locationCountryRepository.findById(id)
                .map(existingCountry -> {
                    LocationCountry updatedCountry = locationCountryMapper.toEntity(locationCountryDTO);
                    updatedCountry.setId(id);
                    updatedCountry.setDeleted(false);
                    LocationCountry savedCountry = locationCountryRepository.save(updatedCountry);
                    LocationCountryDTO result = locationCountryMapper.toDTO(savedCountry);
                    kafkaProducerService.sendLocationCountry(result);
                    return result;
                });
    }

    @Transactional
    public boolean deleteCountry(Long id) {
        return locationCountryRepository.findById(id)
                .map(locationCountry -> {
                    LocationCountryDTO deletedLocationCountry = locationCountryMapper.toDTO(locationCountry);
                    // Soft delete via @SQLDelete
                    locationCountryRepository.delete(locationCountry);
                    deletedLocationCountry.setDeleted(true);
                    kafkaProducerService.sendLocationCountry(deletedLocationCountry);
                    return true;
                })
                .orElse(false);
    }

    private String resolveCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return DEFAULT_CURRENCY_CODE;
        }
        if (!currencyRepository.existsByCode(currencyCode)) {
            throw new IllegalArgumentException("Unknown currency code: " + currencyCode);
        }
        return currencyCode;
    }
}

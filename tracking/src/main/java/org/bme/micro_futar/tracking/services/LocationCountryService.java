package org.bme.micro_futar.tracking.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.tracking.entities.LocationCountry;
import org.bme.micro_futar.tracking.mappers.LocationCountryMapper;
import org.bme.micro_futar.tracking.repositories.LocationCountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationCountryService {

    private final LocationCountryRepository locationCountryRepository;
    private final LocationCountryMapper locationCountryMapper;

    public List<LocationCountryDTO> getAllCountries() {
        return locationCountryRepository.findAll().stream()
                .map(locationCountryMapper::toDTO)
                .toList();
    }

    public Optional<LocationCountryDTO> getCountryById(Long id) {
        return locationCountryRepository.findById(id)
                .map(locationCountryMapper::toDTO);
    }

    @Transactional
    public LocationCountry saveLocationCountry(LocationCountryDTO locationCountryDTO) {
        LocationCountry locationCountry = locationCountryMapper.toEntity(locationCountryDTO);
        return locationCountryRepository.save(locationCountry);
    }
}

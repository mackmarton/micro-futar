package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.entities.LocationCountry;
import org.bme.micro_futar.orders.mappers.LocationCountryMapper;
import org.bme.micro_futar.orders.repositories.LocationCountryRepository;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
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
        LocationCountry locationCountry;

        // Check if entity already exists to avoid optimistic locking issues
        if (locationCountryDTO.getId() != null) {
            Optional<LocationCountry> existing = locationCountryRepository.findById(locationCountryDTO.getId());
            if (existing.isPresent()) {
                locationCountry = existing.get();
                // Update existing entity fields
                locationCountry.setRegionId(locationCountryDTO.getRegionId());
                locationCountry.setName(locationCountryDTO.getName());
            } else {
                locationCountry = locationCountryMapper.toEntity(locationCountryDTO);
            }
        } else {
            locationCountry = locationCountryMapper.toEntity(locationCountryDTO);
        }

        return locationCountryRepository.save(locationCountry);
    }
}

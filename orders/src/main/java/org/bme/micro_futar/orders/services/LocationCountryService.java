package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.LocationCountry;
import org.bme.micro_futar.orders.mappers.LocationCountryMapper;
import org.bme.micro_futar.orders.repositories.LocationCountryRepository;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationCountryService {

    @Autowired
    private LocationCountryRepository locationCountryRepository;

    @Autowired
    private LocationCountryMapper locationCountryMapper;

    public List<LocationCountryDTO> getAllCountries() {
        return locationCountryRepository.findAll().stream()
                .map(locationCountryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LocationCountryDTO> getCountryById(Long id) {
        return locationCountryRepository.findById(id)
                .map(locationCountryMapper::toDTO);
    }

    public LocationCountryDTO createCountry(LocationCountryDTO locationCountryDTO) {
        LocationCountry locationCountry = locationCountryMapper.toEntity(locationCountryDTO);
        LocationCountry savedCountry = locationCountryRepository.save(locationCountry);
        return locationCountryMapper.toDTO(savedCountry);
    }

    public Optional<LocationCountryDTO> updateCountry(Long id, LocationCountryDTO locationCountryDTO) {
        if (locationCountryDTO.getId() != null && !locationCountryDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return locationCountryRepository.findById(id)
                .map(existingCountry -> {
                    LocationCountry updatedCountry = locationCountryMapper.toEntity(locationCountryDTO);
                    LocationCountry savedCountry = locationCountryRepository.save(updatedCountry);
                    return locationCountryMapper.toDTO(savedCountry);
                });
    }

    public boolean deleteCountry(Long id) {
        if (locationCountryRepository.existsById(id)) {
            locationCountryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

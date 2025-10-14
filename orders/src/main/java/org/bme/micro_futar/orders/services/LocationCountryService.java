package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.mappers.LocationCountryMapper;
import org.bme.micro_futar.orders.repositories.LocationCountryRepository;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationCountryService {

    private final LocationCountryRepository locationCountryRepository;
    private final LocationCountryMapper locationCountryMapper;

    public List<LocationCountryDTO> getAllCountries() {
        return locationCountryRepository.findAll().stream()
                .map(locationCountryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LocationCountryDTO> getCountryById(Long id) {
        return locationCountryRepository.findById(id)
                .map(locationCountryMapper::toDTO);
    }
}

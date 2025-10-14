package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.mappers.LocationCityMapper;
import org.bme.micro_futar.orders.repositories.LocationCityRepository;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationCityService {

    private final LocationCityRepository locationCityRepository;
    private final LocationCityMapper locationCityMapper;

    public List<LocationCityDTO> getAllCities() {
        return locationCityRepository.findAll().stream()
                .map(locationCityMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LocationCityDTO> getCityById(Long id) {
        return locationCityRepository.findById(id)
                .map(locationCityMapper::toDTO);
    }
}

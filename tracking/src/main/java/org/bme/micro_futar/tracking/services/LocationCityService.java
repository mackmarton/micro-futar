package org.bme.micro_futar.tracking.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.tracking.entities.LocationCity;
import org.bme.micro_futar.tracking.mappers.LocationCityMapper;
import org.bme.micro_futar.tracking.repositories.LocationCityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationCityService {

    private final LocationCityRepository locationCityRepository;
    private final LocationCityMapper locationCityMapper;

    public List<LocationCityDTO> getAllCities() {
        return locationCityRepository.findAll().stream()
                .map(locationCityMapper::toDTO)
                .toList();
    }

    public Optional<LocationCityDTO> getCityById(Long id) {
        return locationCityRepository.findById(id)
                .map(locationCityMapper::toDTO);
    }

    @Transactional
    public LocationCity saveLocationCity(LocationCityDTO locationCityDTO) {
        LocationCity locationCity = locationCityMapper.toEntity(locationCityDTO);
        return locationCityRepository.save(locationCity);
    }
}

package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.orders.mappers.LocationCityMapper;
import org.bme.micro_futar.orders.repositories.LocationCityRepository;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
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

    public List<LocationCityDTO> getAllCitiesByCountryId(Long countryId) {
        return locationCityRepository.findAllByCountryId(countryId).stream()
                .map(locationCityMapper::toDTO)
                .toList();
    }

    @Transactional
    public LocationCity saveLocationCity(LocationCityDTO locationCityDTO) {
        LocationCity locationCity;

        // Check if entity already exists to avoid optimistic locking issues
        if (locationCityDTO.getId() != null) {
            Optional<LocationCity> existing = locationCityRepository.findById(locationCityDTO.getId());
            if (existing.isPresent()) {
                locationCity = existing.get();
                // Update existing entity fields
                locationCity.setCountryId(locationCityDTO.getCountryId());
                locationCity.setName(locationCityDTO.getName());
            } else {
                locationCity = locationCityMapper.toEntity(locationCityDTO);
            }
        } else {
            locationCity = locationCityMapper.toEntity(locationCityDTO);
        }

        return locationCityRepository.save(locationCity);
    }
}

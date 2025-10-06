package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.orders.mappers.LocationCityMapper;
import org.bme.micro_futar.orders.repositories.LocationCityRepository;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationCityService {

    @Autowired
    private LocationCityRepository locationCityRepository;

    @Autowired
    private LocationCityMapper locationCityMapper;

    public List<LocationCityDTO> getAllCities() {
        return locationCityRepository.findAll().stream()
                .map(locationCityMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LocationCityDTO> getCityById(Long id) {
        return locationCityRepository.findById(id)
                .map(locationCityMapper::toDTO);
    }

    public LocationCityDTO createCity(LocationCityDTO locationCityDTO) {
        LocationCity locationCity = locationCityMapper.toEntity(locationCityDTO);
        LocationCity savedCity = locationCityRepository.save(locationCity);
        return locationCityMapper.toDTO(savedCity);
    }

    public Optional<LocationCityDTO> updateCity(Long id, LocationCityDTO locationCityDTO) {
        if (locationCityDTO.getId() != null && !locationCityDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return locationCityRepository.findById(id)
                .map(existingCity -> {
                    LocationCity updatedCity = locationCityMapper.toEntity(locationCityDTO);
                    LocationCity savedCity = locationCityRepository.save(updatedCity);
                    return locationCityMapper.toDTO(savedCity);
                });
    }

    public boolean deleteCity(Long id) {
        if (locationCityRepository.existsById(id)) {
            locationCityRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

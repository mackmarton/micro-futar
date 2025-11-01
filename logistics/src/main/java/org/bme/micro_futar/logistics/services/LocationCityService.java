package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.LocationCityMapper;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
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
    private final KafkaProducerService kafkaProducerService;

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
    public LocationCityDTO createCity(LocationCityDTO locationCityDTO) {
        LocationCity locationCity = locationCityMapper.toEntity(locationCityDTO);
        LocationCity savedCity = locationCityRepository.save(locationCity);
        LocationCityDTO result = locationCityMapper.toDTO(savedCity);
        kafkaProducerService.sendLocationCity(result);
        return result;
    }

    @Transactional
    public Optional<LocationCityDTO> updateCity(Long id, LocationCityDTO locationCityDTO) {
        if (locationCityDTO.getId() != null && !locationCityDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return locationCityRepository.findById(id)
                .map(existingCity -> {
                    LocationCity updatedCity = locationCityMapper.toEntity(locationCityDTO);
                    LocationCity savedCity = locationCityRepository.save(updatedCity);
                    LocationCityDTO result = locationCityMapper.toDTO(savedCity);
                    kafkaProducerService.sendLocationCity(result);
                    return result;
                });
    }

    @Transactional
    public boolean deleteCity(Long id) {
        if (locationCityRepository.existsById(id)) {
            locationCityRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

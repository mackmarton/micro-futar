package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.LocationCountryMapper;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationCountryService {

    private final LocationCountryRepository locationCountryRepository;
    private final LocationCountryMapper locationCountryMapper;
    private final KafkaProducerService kafkaProducerService;

    public List<LocationCountryDTO> getAllCountries() {
        return locationCountryRepository.findAll().stream()
                .map(locationCountryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LocationCountryDTO> getCountryById(Long id) {
        return locationCountryRepository.findById(id)
                .map(locationCountryMapper::toDTO);
    }

    @Transactional
    public LocationCountryDTO createCountry(LocationCountryDTO locationCountryDTO) {
        LocationCountry locationCountry = locationCountryMapper.toEntity(locationCountryDTO);
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

        return locationCountryRepository.findById(id)
                .map(existingCountry -> {
                    LocationCountry updatedCountry = locationCountryMapper.toEntity(locationCountryDTO);
                    LocationCountry savedCountry = locationCountryRepository.save(updatedCountry);
                    LocationCountryDTO result = locationCountryMapper.toDTO(savedCountry);
                    kafkaProducerService.sendLocationCountry(result);
                    return result;
                });
    }

    @Transactional
    public boolean deleteCountry(Long id) {
        if (locationCountryRepository.existsById(id)) {
            locationCountryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

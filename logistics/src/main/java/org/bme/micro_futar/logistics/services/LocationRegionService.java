package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.LocationRegion;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.LocationRegionMapper;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationRegionService {

    private final LocationRegionRepository locationRegionRepository;
    private final LocationRegionMapper locationRegionMapper;
    private final KafkaProducerService kafkaProducerService;

    public List<LocationRegionDTO> getAllRegions() {
        return locationRegionRepository.findAll().stream()
                .map(locationRegionMapper::toDTO)
                .toList();
    }

    public Optional<LocationRegionDTO> getRegionById(Long id) {
        return locationRegionRepository.findById(id)
                .map(locationRegionMapper::toDTO);
    }

    @Transactional
    public LocationRegionDTO createRegion(LocationRegionDTO locationRegionDTO) {
        LocationRegion locationRegion = locationRegionMapper.toEntity(locationRegionDTO);
        locationRegion.setDeleted(false);
        LocationRegion savedRegion = locationRegionRepository.save(locationRegion);
        LocationRegionDTO result = locationRegionMapper.toDTO(savedRegion);
        kafkaProducerService.sendLocationRegion(result);
        return result;
    }

    @Transactional
    public Optional<LocationRegionDTO> updateRegion(Long id, LocationRegionDTO locationRegionDTO) {
        if (locationRegionDTO.getId() != null && !locationRegionDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return locationRegionRepository.findById(id)
                .map(existingRegion -> {
                    LocationRegion updatedRegion = locationRegionMapper.toEntity(locationRegionDTO);
                    updatedRegion.setId(id);
                    updatedRegion.setDeleted(false);
                    LocationRegion savedRegion = locationRegionRepository.save(updatedRegion);
                    LocationRegionDTO result = locationRegionMapper.toDTO(savedRegion);
                    kafkaProducerService.sendLocationRegion(result);
                    return result;
                });
    }

    @Transactional
    public boolean deleteRegion(Long id) {
        return locationRegionRepository.findById(id)
                .map(locationRegion -> {
                    LocationRegionDTO deletedLocationRegion = locationRegionMapper.toDTO(locationRegion);
                    // Soft delete via @SQLDelete
                    locationRegionRepository.delete(locationRegion);
                    deletedLocationRegion.setDeleted(true);
                    kafkaProducerService.sendLocationRegion(deletedLocationRegion);
                    return true;
                })
                .orElse(false);
    }
}

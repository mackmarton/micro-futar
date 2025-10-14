package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.LocationRegion;
import org.bme.micro_futar.logistics.mappers.LocationRegionMapper;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationRegionService {

    private final LocationRegionRepository locationRegionRepository;
    private final LocationRegionMapper locationRegionMapper;

    public List<LocationRegionDTO> getAllRegions() {
        return locationRegionRepository.findAll().stream()
                .map(locationRegionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LocationRegionDTO> getRegionById(Long id) {
        return locationRegionRepository.findById(id)
                .map(locationRegionMapper::toDTO);
    }

    @Transactional
    public LocationRegionDTO createRegion(LocationRegionDTO locationRegionDTO) {
        LocationRegion locationRegion = locationRegionMapper.toEntity(locationRegionDTO);
        LocationRegion savedRegion = locationRegionRepository.save(locationRegion);
        return locationRegionMapper.toDTO(savedRegion);
    }

    @Transactional
    public Optional<LocationRegionDTO> updateRegion(Long id, LocationRegionDTO locationRegionDTO) {
        if (locationRegionDTO.getId() != null && !locationRegionDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return locationRegionRepository.findById(id)
                .map(existingRegion -> {
                    LocationRegion updatedRegion = locationRegionMapper.toEntity(locationRegionDTO);
                    LocationRegion savedRegion = locationRegionRepository.save(updatedRegion);
                    return locationRegionMapper.toDTO(savedRegion);
                });
    }

    @Transactional
    public boolean deleteRegion(Long id) {
        if (locationRegionRepository.existsById(id)) {
            locationRegionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

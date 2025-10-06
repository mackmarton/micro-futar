package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.LocationRegion;
import org.bme.micro_futar.orders.mappers.LocationRegionMapper;
import org.bme.micro_futar.orders.repositories.LocationRegionRepository;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationRegionService {

    @Autowired
    private LocationRegionRepository locationRegionRepository;

    @Autowired
    private LocationRegionMapper locationRegionMapper;

    public List<LocationRegionDTO> getAllRegions() {
        return locationRegionRepository.findAll().stream()
                .map(locationRegionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LocationRegionDTO> getRegionById(Long id) {
        return locationRegionRepository.findById(id)
                .map(locationRegionMapper::toDTO);
    }

    public LocationRegionDTO createRegion(LocationRegionDTO locationRegionDTO) {
        LocationRegion locationRegion = locationRegionMapper.toEntity(locationRegionDTO);
        LocationRegion savedRegion = locationRegionRepository.save(locationRegion);
        return locationRegionMapper.toDTO(savedRegion);
    }

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

    public boolean deleteRegion(Long id) {
        if (locationRegionRepository.existsById(id)) {
            locationRegionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

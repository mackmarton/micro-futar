package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.entities.LocationRegion;
import org.bme.micro_futar.orders.mappers.LocationRegionMapper;
import org.bme.micro_futar.orders.repositories.LocationRegionRepository;
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
    public LocationRegion saveLocationRegion(LocationRegionDTO locationRegionDTO) {
        LocationRegion locationRegion = locationRegionMapper.toEntity(locationRegionDTO);
        return locationRegionRepository.save(locationRegion);
    }
}

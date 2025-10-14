package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.mappers.LocationRegionMapper;
import org.bme.micro_futar.orders.repositories.LocationRegionRepository;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.springframework.stereotype.Service;

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
}

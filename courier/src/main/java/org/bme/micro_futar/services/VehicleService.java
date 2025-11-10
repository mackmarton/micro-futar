package org.bme.micro_futar.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.entities.Vehicle;
import org.bme.micro_futar.mappers.VehicleMapper;
import org.bme.micro_futar.repositories.VehicleRepository;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleDTO save(VehicleDTO vehicleDTO) {
        log.info("Saving vehicle: {}", vehicleDTO);
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(savedVehicle);
    }

    @Transactional(readOnly = true)
    public Optional<VehicleDTO> findById(Long id) {
        log.debug("Finding vehicle by id: {}", id);
        return vehicleRepository.findById(id)
                .map(vehicleMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> findAll() {
        log.debug("Finding all vehicles");
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toDTO)
                .toList();
    }
}

package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.Vehicle;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.VehicleMapper;
import org.bme.micro_futar.logistics.repositories.VehicleRepository;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final KafkaProducerService kafkaProducerService;

    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toDTO)
                .toList();
    }

    public Optional<VehicleDTO> getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .map(vehicleMapper::toDTO);
    }

    @Transactional
    public VehicleDTO createVehicle(VehicleDTO vehicleDTO) {
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDTO);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        VehicleDTO resultDTO = vehicleMapper.toDTO(savedVehicle);
        kafkaProducerService.sendVehicle(resultDTO);
        return resultDTO;
    }

    @Transactional
    public Optional<VehicleDTO> updateVehicle(Long id, VehicleDTO vehicleDTO) {
        if (vehicleDTO.getId() != null && !vehicleDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return vehicleRepository.findById(id)
                .map(_ -> {
                    Vehicle updatedVehicle = vehicleMapper.toEntity(vehicleDTO);
                    updatedVehicle.setId(id);
                    Vehicle savedVehicle = vehicleRepository.save(updatedVehicle);
                    VehicleDTO resultDTO = vehicleMapper.toDTO(savedVehicle);
                    kafkaProducerService.sendVehicle(resultDTO);
                    return resultDTO;
                });
    }

    @Transactional
    public boolean deleteVehicle(Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

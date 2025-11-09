package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.CarrierDTO;
import org.bme.micro_futar.logistics.entities.Carrier;
import org.bme.micro_futar.logistics.mappers.CarrierMapper;
import org.bme.micro_futar.logistics.repositories.CarrierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierRepository carrierRepository;
    private final CarrierMapper carrierMapper;

    public List<CarrierDTO> getAllCarriers() {
        return carrierRepository.findAll().stream()
                .map(carrierMapper::toDTO)
                .toList();
    }

    public Optional<CarrierDTO> getCarrierById(Long id) {
        return carrierRepository.findById(id)
                .map(carrierMapper::toDTO);
    }

    @Transactional
    public CarrierDTO createCarrier(CarrierDTO carrierDTO) {
        Carrier carrier = carrierMapper.toEntity(carrierDTO);
        Carrier savedCarrier = carrierRepository.save(carrier);
        return carrierMapper.toDTO(savedCarrier);
    }

    @Transactional
    public Optional<CarrierDTO> updateCarrier(Long id, CarrierDTO carrierDTO) {
        if (carrierDTO.getId() != null && !carrierDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return carrierRepository.findById(id)
                .map(_ -> {
                    Carrier updatedCarrier = carrierMapper.toEntity(carrierDTO);
                    updatedCarrier.setId(id);
                    Carrier savedCarrier = carrierRepository.save(updatedCarrier);
                    return carrierMapper.toDTO(savedCarrier);
                });
    }

    @Transactional
    public boolean deleteCarrier(Long id) {
        if (carrierRepository.existsById(id)) {
            carrierRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<CarrierDTO> getCarriersByDepoIdAndType(Long depoId, org.bme.micro_futar.shared.enums.CarrierType carrierType) {
        return carrierRepository.findByDepoIdAndCarrierType(depoId, carrierType).stream()
                .map(carrierMapper::toDTO)
                .toList();
    }
}

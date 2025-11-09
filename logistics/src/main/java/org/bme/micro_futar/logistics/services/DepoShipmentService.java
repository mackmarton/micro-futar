package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.DepoShipment;
import org.bme.micro_futar.logistics.mappers.DepoShipmentMapper;
import org.bme.micro_futar.logistics.repositories.DepoShipmentRepository;
import org.bme.micro_futar.shared.dtos.DepoShipmentDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepoShipmentService {

    private final DepoShipmentRepository depoShipmentRepository;
    private final DepoShipmentMapper depoShipmentMapper;

    public DepoShipmentDTO saveDepoShipment(DepoShipmentDTO depoShipmentDTO) {
        DepoShipment depoShipment = depoShipmentMapper.fromDto(depoShipmentDTO);
        DepoShipment savedDepoShipment = depoShipmentRepository.save(depoShipment);
        return depoShipmentMapper.toDto(savedDepoShipment);
    }
}


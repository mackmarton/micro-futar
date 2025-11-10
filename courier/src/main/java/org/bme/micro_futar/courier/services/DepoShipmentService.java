package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.DepoShipment;
import org.bme.micro_futar.courier.mappers.DepoShipmentMapper;
import org.bme.micro_futar.courier.repositories.DepoShipmentRepository;
import org.bme.micro_futar.shared.dtos.DepoShipmentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepoShipmentService {

    private final DepoShipmentRepository depoShipmentRepository;
    private final DepoShipmentMapper depoShipmentMapper;

    @Transactional
    public DepoShipmentDTO save(DepoShipmentDTO depoShipmentDTO) {
        log.info("Saving depoShipment: {}", depoShipmentDTO);
        DepoShipment depoShipment = depoShipmentMapper.fromDto(depoShipmentDTO);
        DepoShipment savedDepoShipment = depoShipmentRepository.save(depoShipment);
        return depoShipmentMapper.toDto(savedDepoShipment);
    }

    @Transactional(readOnly = true)
    public Optional<DepoShipmentDTO> findById(Long id) {
        log.debug("Finding depoShipment by id: {}", id);
        return depoShipmentRepository.findById(id)
                .map(depoShipmentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<DepoShipmentDTO> findAll() {
        log.debug("Finding all depoShipments");
        return depoShipmentRepository.findAll().stream()
                .map(depoShipmentMapper::toDto)
                .toList();
    }
}

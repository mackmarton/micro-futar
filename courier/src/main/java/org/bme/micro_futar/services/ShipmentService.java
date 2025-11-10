package org.bme.micro_futar.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.entities.Shipment;
import org.bme.micro_futar.mappers.ShipmentMapper;
import org.bme.micro_futar.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    @Transactional
    public ShipmentDTO save(ShipmentDTO shipmentDTO) {
        log.info("Saving shipment: {}", shipmentDTO);
        Shipment shipment = shipmentMapper.toEntity(shipmentDTO);
        Shipment savedShipment = shipmentRepository.save(shipment);
        return shipmentMapper.toDTO(savedShipment);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentDTO> findById(Long id) {
        log.debug("Finding shipment by id: {}", id);
        return shipmentRepository.findById(id)
                .map(shipmentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ShipmentDTO> findAll() {
        log.debug("Finding all shipments");
        return shipmentRepository.findAll().stream()
                .map(shipmentMapper::toDTO)
                .toList();
    }
}


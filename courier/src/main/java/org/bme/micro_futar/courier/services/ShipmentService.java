package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.Shipment;
import org.bme.micro_futar.courier.mappers.ShipmentMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentMapper shipmentMapper;
    private final ShipmentRepository shipmentRepository;

    /** Upserts the read-only copy of the shipment that orders owns; courier never publishes shipments. */
    @Transactional
    public ShipmentDTO saveWithoutTopicSend(ShipmentDTO shipmentDTO) {
        log.info("Saving shipment: {}", shipmentDTO);
        Shipment shipment = shipmentMapper.toEntity(shipmentDTO);
        Shipment savedShipment = shipmentRepository.save(shipment);
        return shipmentMapper.toDTO(savedShipment);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentDTO> findById(Long id) {
        return shipmentRepository.findById(id)
                .map(shipmentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ShipmentDTO> findAll() {
        return shipmentRepository.findAll().stream()
                .map(shipmentMapper::toDTO)
                .toList();
    }
}


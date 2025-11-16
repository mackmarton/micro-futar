package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.bme.micro_futar.courier.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteCourierService {

    private final ShipmentRouteCourierRepository shipmentRouteCourierRepository;
    private final ShipmentRouteCourierMapper shipmentRouteCourierMapper;

    @Transactional
    public ShipmentRouteCourierDTO save(ShipmentRouteCourierDTO shipmentRouteCourierDTO) {
        log.info("Saving shipmentRouteCourier: {}", shipmentRouteCourierDTO);
        ShipmentRouteCourier shipmentRouteCourier = shipmentRouteCourierMapper.toEntity(shipmentRouteCourierDTO);
        ShipmentRouteCourier savedShipmentRouteCourier = shipmentRouteCourierRepository.save(shipmentRouteCourier);
        return shipmentRouteCourierMapper.toDTO(savedShipmentRouteCourier);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentRouteCourierDTO> findById(Long id) {
        log.debug("Finding shipmentRouteCourier by id: {}", id);
        return shipmentRouteCourierRepository.findById(id)
                .map(shipmentRouteCourierMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ShipmentRouteCourierDTO> findAll() {
        log.debug("Finding all shipmentRouteCouriers");
        return shipmentRouteCourierRepository.findAll().stream()
                .map(shipmentRouteCourierMapper::toDTO)
                .toList();
    }
}


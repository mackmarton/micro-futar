package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.ShipmentRouteCarrier;
import org.bme.micro_futar.courier.mappers.ShipmentRouteCarrierMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRouteCarrierRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCarrierDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteCarrierService {

    private final ShipmentRouteCarrierRepository shipmentRouteCarrierRepository;
    private final ShipmentRouteCarrierMapper shipmentRouteCarrierMapper;

    @Transactional
    public ShipmentRouteCarrierDTO save(ShipmentRouteCarrierDTO shipmentRouteCarrierDTO) {
        log.info("Saving shipmentRouteCarrier: {}", shipmentRouteCarrierDTO);
        ShipmentRouteCarrier shipmentRouteCarrier = shipmentRouteCarrierMapper.toEntity(shipmentRouteCarrierDTO);
        ShipmentRouteCarrier savedShipmentRouteCarrier = shipmentRouteCarrierRepository.save(shipmentRouteCarrier);
        return shipmentRouteCarrierMapper.toDTO(savedShipmentRouteCarrier);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentRouteCarrierDTO> findById(Long id) {
        log.debug("Finding shipmentRouteCarrier by id: {}", id);
        return shipmentRouteCarrierRepository.findById(id)
                .map(shipmentRouteCarrierMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ShipmentRouteCarrierDTO> findAll() {
        log.debug("Finding all shipmentRouteCarriers");
        return shipmentRouteCarrierRepository.findAll().stream()
                .map(shipmentRouteCarrierMapper::toDTO)
                .toList();
    }
}


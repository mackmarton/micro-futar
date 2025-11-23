package org.bme.micro_futar.tracking.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.tracking.entities.ShipmentRoute;
import org.bme.micro_futar.tracking.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.tracking.repositories.ShipmentRouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteService {

    private final ShipmentRouteMapper shipmentRouteMapper;
    private final ShipmentRouteRepository shipmentRouteRepository;

    public Optional<ShipmentRouteDTO> findById(Long id) {
        return shipmentRouteRepository.findById(id)
                .map(shipmentRouteMapper::toDTO);
    }

    public List<ShipmentRouteDTO> findAll() {
        return shipmentRouteRepository.findAll().stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    @Transactional
    public ShipmentRouteDTO save(ShipmentRouteDTO shipmentRouteDTO) {
        log.info("Saving shipmentRoute: {}", shipmentRouteDTO);
        ShipmentRoute shipmentRoute = shipmentRouteMapper.toEntity(shipmentRouteDTO);
        ShipmentRoute savedShipmentRoute = shipmentRouteRepository.save(shipmentRoute);
        return shipmentRouteMapper.toDTO(savedShipmentRoute);
    }

    public List<ShipmentRouteDTO> findAllFulfilledByShipmentId(Long shipmentId) {
        return shipmentRouteRepository.findAllByShipmentIdEqualsAndFulfillmentTimeIsNotNull(shipmentId).stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }
}


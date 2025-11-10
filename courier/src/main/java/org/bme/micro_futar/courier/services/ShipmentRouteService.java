package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.ShipmentRoute;
import org.bme.micro_futar.courier.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteService {

    private final ShipmentRouteRepository shipmentRouteRepository;
    private final ShipmentRouteMapper shipmentRouteMapper;

    @Transactional
    public ShipmentRouteDTO save(ShipmentRouteDTO shipmentRouteDTO) {
        log.info("Saving shipmentRoute: {}", shipmentRouteDTO);
        ShipmentRoute shipmentRoute = shipmentRouteMapper.toEntity(shipmentRouteDTO);
        ShipmentRoute savedShipmentRoute = shipmentRouteRepository.save(shipmentRoute);
        return shipmentRouteMapper.toDTO(savedShipmentRoute);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentRouteDTO> findById(Long id) {
        log.debug("Finding shipmentRoute by id: {}", id);
        return shipmentRouteRepository.findById(id)
                .map(shipmentRouteMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ShipmentRouteDTO> findAll() {
        log.debug("Finding all shipmentRoutes");
        return shipmentRouteRepository.findAll().stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }
}


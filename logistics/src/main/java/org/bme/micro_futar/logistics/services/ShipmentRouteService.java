package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipmentRouteService {

    private final ShipmentRouteRepository shipmentRouteRepository;
    private final ShipmentRouteMapper shipmentRouteMapper;

    public List<ShipmentRouteDTO> getAllShipmentRoutes() {
        return shipmentRouteRepository.findAll().stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    public Optional<ShipmentRouteDTO> getShipmentRouteById(Long id) {
        return shipmentRouteRepository.findById(id)
                .map(shipmentRouteMapper::toDTO);
    }

    public void saveAll(List<ShipmentRouteDTO> shipmentRouteDTOs){
        shipmentRouteRepository.saveAll(shipmentRouteMapper.toEntityList(shipmentRouteDTOs));
    }

    public List<ShipmentRouteDTO> getDeliveryRoutes(Long depoId) {
        return shipmentRouteRepository.findDeliveryRoutes(depoId).stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    public List<ShipmentRouteDTO> getPickupRoutes(Long depoId) {
        return shipmentRouteRepository.findPickupRoutes(depoId).stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }
}

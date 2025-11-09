package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipmentRouteService {

    private final ShipmentRouteRepository shipmentRouteRepository;
    private final ShipmentRouteMapper shipmentRouteMapper;
    private final KafkaProducerService kafkaProducerService;

    public List<ShipmentRouteDTO> getAllShipmentRoutes() {
        return shipmentRouteRepository.findAll().stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    public Optional<ShipmentRouteDTO> getShipmentRouteById(Long id) {
        return shipmentRouteRepository.findById(id)
                .map(shipmentRouteMapper::toDTO);
    }

    @Transactional
    public void saveAll(List<ShipmentRouteDTO> shipmentRouteDTOs){
        shipmentRouteRepository.saveAll(shipmentRouteMapper.toEntityList(shipmentRouteDTOs));
        shipmentRouteDTOs.forEach(kafkaProducerService::sendShipmentRoute);
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

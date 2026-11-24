package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteService {

    private final ShipmentRouteRepository shipmentRouteRepository;
    private final ShipmentRouteMapper shipmentRouteMapper;
    private final KafkaProducerService kafkaProducerService;
    private final ApplicationEventPublisher eventPublisher;
    private final ShipmentManualReviewService shipmentManualReviewService;

    public List<ShipmentRouteDTO> getAllShipmentRoutes() {
        return shipmentRouteRepository.findAll().stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    public Optional<ShipmentRouteDTO> getShipmentRouteById(Long id) {
        return shipmentRouteRepository.findById(id)
                .map(shipmentRouteMapper::toDTO);
    }

    public List<ShipmentRouteDTO> getDeliveryRoutes(Long depoId, LocalDate today) {
        return shipmentRouteRepository.findDeliveryRoutes(depoId, today).stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    public List<ShipmentRouteDTO> getPickupRoutes(Long depoId, LocalDate today) {
        return shipmentRouteRepository.findPickupRoutes(depoId, today).stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    public List<ShipmentRouteDTO> getCrossDepoRoutes(Long depoId, LocalDate today) {
        return shipmentRouteRepository.findCrossDepoRoutesForDepo(depoId, today).stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    @Transactional
    public void saveAll(List<ShipmentRouteDTO> shipmentRouteDTOs) {
        List<ShipmentRoute> savedShipmentRoutes = shipmentRouteRepository.saveAll(shipmentRouteMapper.toEntityList(shipmentRouteDTOs));
        shipmentRouteMapper.toDTOList(savedShipmentRoutes).forEach(kafkaProducerService::sendShipmentRoute);
    }

    /** Sets the fulfillment time reported by courier and republishes the route; repeated events are ignored. */
    @Transactional
    public void applyFulfilled(ShipmentRouteFulfilledEventDTO event) {
        Optional<ShipmentRoute> found = shipmentRouteRepository.findById(event.getShipmentRouteId());
        if (found.isEmpty()) {
            log.warn("Ignoring fulfilled event for unknown shipmentRoute {}", event.getShipmentRouteId());
            return;
        }
        ShipmentRoute route = found.get();
        if (route.getFulfillmentTime() != null) {
            return;
        }
        route.setFulfillmentTime(event.getFulfilledAt());
        // Sent to Kafka by KafkaProducerService only after this transaction commits.
        eventPublisher.publishEvent(shipmentRouteMapper.toDTO(shipmentRouteRepository.save(route)));
        if (route.isCanceled()) {
            // The courier's report is a fact and is kept, but the canceled shipment physically moved on.
            shipmentManualReviewService.flag(route.getShipmentId(), "Leg fulfilled after cancellation");
        }
    }
}

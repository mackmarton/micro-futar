package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.ShipmentRoute;
import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDeliveredEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteService {

    private final ShipmentNotificationService shipmentNotificationService;
    private final ShipmentRouteMapper shipmentRouteMapper;
    private final KafkaProducerService kafkaProducerService;
    private final ShipmentRouteRepository shipmentRouteRepository;

    public Optional<ShipmentRouteDTO> findById(Long id) {
        log.debug("Finding shipmentRoute by id: {}", id);
        return shipmentRouteRepository.findById(id)
                .map(shipmentRouteMapper::toDTO);
    }

    public List<ShipmentRouteDTO> findAll() {
        log.debug("Finding all shipmentRoutes");
        return shipmentRouteRepository.findAll().stream()
                .map(shipmentRouteMapper::toDTO)
                .toList();
    }

    /**
     * Reports the leg as fulfilled to logistics, which owns the route. On the delivery leg it also reports the
     * delivery to orders, which owns the shipment. Callers guard against fulfilling a leg twice.
     */
    public void fulfillShipmentRoute(long shipmentRouteId, ZonedDateTime fulfilledAt) {
        var shipmentRoute = findById(shipmentRouteId).orElseThrow();
        kafkaProducerService.sendShipmentRouteFulfilled(ShipmentRouteFulfilledEventDTO.builder()
                .shipmentRouteId(shipmentRouteId)
                .shipmentId(shipmentRoute.getShipmentId())
                .fulfilledAt(fulfilledAt)
                .build());
        if (shipmentRoute.getDestinationAddress() != null) {
            kafkaProducerService.sendShipmentDelivered(ShipmentDeliveredEventDTO.builder()
                    .shipmentId(shipmentRoute.getShipmentId())
                    .deliveredAt(fulfilledAt)
                    .build());
            shipmentNotificationService.notifyDelivered(shipmentRoute);
        }
    }

    /** Upserts the read-only copy of the route owned by logistics; courier never publishes routes. */
    @Transactional
    public ShipmentRouteDTO saveWithoutTopicSend(ShipmentRouteDTO shipmentRouteDTO) {
        log.info("Saving shipmentRoute: {}", shipmentRouteDTO);
        ShipmentRoute shipmentRoute = shipmentRouteMapper.toEntity(shipmentRouteDTO);
        ShipmentRoute savedShipmentRoute = shipmentRouteRepository.save(shipmentRoute);
        return shipmentRouteMapper.toDTO(savedShipmentRoute);
    }
}


package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.ShipmentRoute;
import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteService {

    private final ApplicationContext applicationContext;
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

    public void fulfillShipmentRoute(long shipmentRouteId) {
        var shipmentRoute = findById(shipmentRouteId).orElseThrow();
        shipmentRoute.setFulfillmentTime(Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        save(shipmentRoute);
    }

    public ShipmentRouteDTO save(ShipmentRouteDTO shipmentRouteDTO) {
        var self = applicationContext.getBean(ShipmentRouteService.class);
        var savedShipmentRouteDTO = self.saveWithoutTopicSend(shipmentRouteDTO);
        kafkaProducerService.sendShipmentRoute(savedShipmentRouteDTO);
        return savedShipmentRouteDTO;
    }

    @Transactional
    public ShipmentRouteDTO saveWithoutTopicSend(ShipmentRouteDTO shipmentRouteDTO) {
        log.info("Saving shipmentRoute: {}", shipmentRouteDTO);
        ShipmentRoute shipmentRoute = shipmentRouteMapper.toEntity(shipmentRouteDTO);
        ShipmentRoute savedShipmentRoute = shipmentRouteRepository.save(shipmentRoute);
        return shipmentRouteMapper.toDTO(savedShipmentRoute);
    }
}


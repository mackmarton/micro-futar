package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.exceptions.UnauthorizedException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteCourierService {

    private final CourierService courierService;
    private final ApplicationContext applicationContext;
    private final KafkaProducerService kafkaProducerService;
    private final ShipmentRouteService shipmentRouteService;
    private final ShipmentNotificationService shipmentNotificationService;
    private final ShipmentRouteCourierMapper shipmentRouteCourierMapper;
    private final ShipmentRouteCourierRepository shipmentRouteCourierRepository;

    public Optional<ShipmentRouteCourierDTO> findById(Long id) {
        return shipmentRouteCourierRepository.findById(id)
                .map(shipmentRouteCourierMapper::toDTO);
    }

    public List<ShipmentRouteCourierDTO> findAll() {
        return shipmentRouteCourierRepository.findAll().stream()
                .map(shipmentRouteCourierMapper::toDTO)
                .toList();
    }

    public List<ShipmentRouteCourierDTO> findAllForCourierForCurrentDay(Authentication authentication) {
        Long courierId = getCourierIdByAuthentication(authentication);
        return shipmentRouteCourierRepository.findAllByCourierIdAndDateAssignedFor(courierId, LocalDate.now()).stream()
                .map(shipmentRouteCourierMapper::toDTO)
                .toList();
    }

    public List<ShipmentRouteCourierDTO> findAllDeliveriesForCourierForCurrentDay(Authentication authentication) {
        Long courierId = getCourierIdByAuthentication(authentication);
        return shipmentRouteCourierRepository.findAllDeliveriesByCourierIdAndDateAssignedFor(courierId, LocalDate.now()).stream()
                .map(shipmentRouteCourierMapper::toDTO)
                .toList();
    }

    public List<ShipmentRouteCourierDTO> findAllPickedUpAssignmentsForCourierForCurrentDay(Authentication authentication) {
        Long courierId = getCourierIdByAuthentication(authentication);
        return shipmentRouteCourierRepository.findAllPickedUpParcelsByCourierIdAndDateAssignedFor(courierId, LocalDate.now()).stream()
                .map(shipmentRouteCourierMapper::toDTO)
                .toList();
    }

    public void pickUpAllDeliveryShipmentsForCurrentDay(Authentication authentication) {
        List<ShipmentRouteCourierDTO> shipmentRouteCouriers = findAllDeliveriesForCourierForCurrentDay(authentication);
        for (var assignment : shipmentRouteCouriers) {
            markPickedUp(assignment);
        }
    }

    public void pickUpParcel(Long id, Authentication authentication) {
        markPickedUp(findOwnAssignment(id, authentication));
    }

    private void markPickedUp(ShipmentRouteCourierDTO shipmentRouteCourier) {
        shipmentRouteCourier.setPickedUpForDelivery(true);
        save(shipmentRouteCourier);
        shipmentRouteService.findById(shipmentRouteCourier.getShipmentRouteId())
                .ifPresent(route -> shipmentNotificationService.notifyOutForDelivery(route, shipmentRouteCourier.getId()));
    }

    public void fulfillAllPickupsForCurrentDay(Authentication authentication) {
        List<ShipmentRouteCourierDTO> shipmentRouteCouriers = findAllPickedUpAssignmentsForCourierForCurrentDay(authentication);
        for (var assignment : shipmentRouteCouriers) {
            shipmentRouteService.fulfillShipmentRoute(assignment.getShipmentRouteId());
        }
    }

    public void fulfillAssignment(Long id, Authentication authentication) {
        var shipmentRouteCourier = findOwnAssignment(id, authentication);
        shipmentRouteService.fulfillShipmentRoute(shipmentRouteCourier.getShipmentRouteId());
    }

    public void failAssignment(Long id, Authentication authentication) {
        var shipmentRouteCourier = findOwnAssignment(id, authentication);
        shipmentRouteCourier.setFailed(true);
        save(shipmentRouteCourier);
        shipmentRouteService.findById(shipmentRouteCourier.getShipmentRouteId())
                .ifPresent(route -> shipmentNotificationService.notifyDeliveryFailed(route, shipmentRouteCourier.getId()));
    }

    public ShipmentRouteCourierDTO save(ShipmentRouteCourierDTO shipmentRouteCourierDTO) {
        var self = applicationContext.getBean(ShipmentRouteCourierService.class);
        var savedShipmentRouteCourierDTO = self.saveWithoutTopicSend(shipmentRouteCourierDTO);
        kafkaProducerService.sendShipmentRouteCourier(savedShipmentRouteCourierDTO);
        return savedShipmentRouteCourierDTO;
    }

    @Transactional
    public ShipmentRouteCourierDTO saveWithoutTopicSend(ShipmentRouteCourierDTO shipmentRouteCourierDTO) {
        log.info("Saving shipmentRouteCourier: {}", shipmentRouteCourierDTO);
        ShipmentRouteCourier shipmentRouteCourier = shipmentRouteCourierMapper.toEntity(shipmentRouteCourierDTO);
        ShipmentRouteCourier savedShipmentRouteCourier = shipmentRouteCourierRepository.save(shipmentRouteCourier);
        return shipmentRouteCourierMapper.toDTO(savedShipmentRouteCourier);
    }

    private ShipmentRouteCourierDTO findOwnAssignment(Long id, Authentication authentication) {
        Long courierId = getCourierIdByAuthentication(authentication);
        var shipmentRouteCourier = findById(id).orElseThrow();
        if (!Objects.equals(shipmentRouteCourier.getCourierId(), courierId)) {
            throw new UnauthorizedException();
        }
        return shipmentRouteCourier;
    }

    private Long getCourierIdByAuthentication(Authentication authentication) {
        String courierEmail = extractUserEmail(authentication);
        return courierService.findIdByEmail(courierEmail);
    }

    private String extractUserEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String email = jwtAuthenticationToken.getToken().getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        return null;
    }
}


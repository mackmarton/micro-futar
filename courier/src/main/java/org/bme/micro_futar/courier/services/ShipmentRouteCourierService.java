package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.config.auth.VerifiedEmails;
import org.bme.micro_futar.courier.entities.AssignmentProgress;
import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.courier.repositories.AssignmentProgressRepository;
import org.bme.micro_futar.courier.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.enums.AssignmentStatus;
import org.bme.micro_futar.shared.exceptions.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteCourierService {

    private final CourierService courierService;
    private final AssignmentProgressRepository assignmentProgressRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ShipmentRouteService shipmentRouteService;
    private final ShipmentNotificationService shipmentNotificationService;
    private final ShipmentRouteCourierMapper shipmentRouteCourierMapper;
    private final ShipmentRouteCourierRepository shipmentRouteCourierRepository;

    public Optional<ShipmentRouteCourierDTO> findById(Long id) {
        return shipmentRouteCourierRepository.findById(id)
                .map(entity -> toDTOs(List.of(entity)).getFirst());
    }

    public List<ShipmentRouteCourierDTO> findAll() {
        return toDTOs(shipmentRouteCourierRepository.findAll());
    }

    public List<ShipmentRouteCourierDTO> findAllForCourierForCurrentDay(Authentication authentication) {
        Long courierId = getCourierIdByAuthentication(authentication);
        return toDTOs(shipmentRouteCourierRepository.findAllByCourierIdAndDateAssignedForAndCanceledFalse(courierId, LocalDate.now()));
    }

    public List<ShipmentRouteCourierDTO> findAllDeliveriesForCourierForCurrentDay(Authentication authentication) {
        Long courierId = getCourierIdByAuthentication(authentication);
        return toDTOs(shipmentRouteCourierRepository.findAllDeliveriesByCourierIdAndDateAssignedFor(courierId, LocalDate.now()));
    }

    public List<ShipmentRouteCourierDTO> findAllPickedUpAssignmentsForCourierForCurrentDay(Authentication authentication) {
        Long courierId = getCourierIdByAuthentication(authentication);
        return toDTOs(shipmentRouteCourierRepository.findAllPickedUpParcelsByCourierIdAndDateAssignedFor(courierId, LocalDate.now()));
    }

    @Transactional
    public void pickUpAllDeliveryShipmentsForCurrentDay(Authentication authentication) {
        List<ShipmentRouteCourierDTO> shipmentRouteCouriers = findAllDeliveriesForCourierForCurrentDay(authentication);
        for (var assignment : shipmentRouteCouriers) {
            markPickedUp(assignment);
        }
    }

    @Transactional
    public void pickUpParcel(Long id, Authentication authentication) {
        markPickedUp(findOwnAssignment(id, authentication));
    }

    private void markPickedUp(ShipmentRouteCourierDTO assignment) {
        if (assignment.isCanceled()) {
            log.warn("Not picking up canceled assignment {}", assignment.getId());
            return;
        }
        AssignmentProgress progress = progressOf(assignment.getId());
        if (progress.getPickedUpAt() != null) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now();
        progress.setPickedUpAt(now);
        assignmentProgressRepository.save(progress);
        kafkaProducerService.sendAssignmentStatus(statusEvent(assignment, AssignmentStatus.PICKED_UP, now));
        shipmentRouteService.findById(assignment.getShipmentRouteId())
                .ifPresent(route -> shipmentNotificationService.notifyOutForDelivery(route, assignment.getId()));
    }

    @Transactional
    public void fulfillAllPickupsForCurrentDay(Authentication authentication) {
        List<ShipmentRouteCourierDTO> shipmentRouteCouriers = findAllPickedUpAssignmentsForCourierForCurrentDay(authentication);
        for (var assignment : shipmentRouteCouriers) {
            fulfill(assignment);
        }
    }

    @Transactional
    public void fulfillAssignment(Long id, Authentication authentication) {
        fulfill(findOwnAssignment(id, authentication));
    }

    private void fulfill(ShipmentRouteCourierDTO assignment) {
        AssignmentProgress progress = progressOf(assignment.getId());
        if (progress.getFulfilledAt() != null || rejectedByCancellation(assignment, progress, "fulfill")) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now();
        progress.setFulfilledAt(now);
        assignmentProgressRepository.save(progress);
        shipmentRouteService.fulfillShipmentRoute(assignment.getShipmentRouteId(), now);
    }

    @Transactional
    public void failAssignment(Long id, Authentication authentication) {
        var assignment = findOwnAssignment(id, authentication);
        AssignmentProgress progress = progressOf(assignment.getId());
        if (progress.getFailedAt() != null || rejectedByCancellation(assignment, progress, "fail")) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now();
        progress.setFailedAt(now);
        assignmentProgressRepository.save(progress);
        kafkaProducerService.sendAssignmentStatus(statusEvent(assignment, AssignmentStatus.FAILED, now));
        shipmentRouteService.findById(assignment.getShipmentRouteId())
                .ifPresent(route -> shipmentNotificationService.notifyDeliveryFailed(route, assignment.getId()));
    }

    /**
     * A canceled assignment can no longer be fulfilled or failed, unless the courier already picked the parcel up:
     * it is physically with them, so what they report is kept (and logistics flags the shipment for manual handling).
     */
    private static boolean rejectedByCancellation(ShipmentRouteCourierDTO assignment, AssignmentProgress progress,
                                                  String action) {
        if (!assignment.isCanceled()) {
            return false;
        }
        if (progress.getPickedUpAt() != null) {
            log.warn("Canceled assignment {} was already picked up, allowing it to {}", assignment.getId(), action);
            return false;
        }
        log.warn("Not able to {} canceled assignment {} that was never picked up", action, assignment.getId());
        return true;
    }

    /** Upserts the read-only copy of the assignment owned by logistics; courier never publishes assignments. */
    @Transactional
    public ShipmentRouteCourierDTO saveWithoutTopicSend(ShipmentRouteCourierDTO shipmentRouteCourierDTO) {
        log.info("Saving shipmentRouteCourier: {}", shipmentRouteCourierDTO);
        ShipmentRouteCourier shipmentRouteCourier = shipmentRouteCourierMapper.toEntity(shipmentRouteCourierDTO);
        ShipmentRouteCourier savedShipmentRouteCourier = shipmentRouteCourierRepository.save(shipmentRouteCourier);
        return toDTOs(List.of(savedShipmentRouteCourier)).getFirst();
    }

    private AssignmentProgress progressOf(Long assignmentId) {
        return assignmentProgressRepository.findById(assignmentId).orElseGet(() -> {
            AssignmentProgress progress = new AssignmentProgress();
            progress.setAssignmentId(assignmentId);
            return progress;
        });
    }

    private static AssignmentStatusEventDTO statusEvent(ShipmentRouteCourierDTO assignment, AssignmentStatus status,
                                                        ZonedDateTime at) {
        return AssignmentStatusEventDTO.builder()
                .assignmentId(assignment.getId())
                .shipmentRouteId(assignment.getShipmentRouteId())
                .status(status)
                .occurredAt(at)
                .build();
    }

    /** Merges the courier-owned progress into the assignments received from logistics. */
    private List<ShipmentRouteCourierDTO> toDTOs(List<ShipmentRouteCourier> assignments) {
        Map<Long, AssignmentProgress> progressById = assignmentProgressRepository
                .findAllById(assignments.stream().map(ShipmentRouteCourier::getId).toList()).stream()
                .collect(Collectors.toMap(AssignmentProgress::getAssignmentId, Function.identity()));
        return assignments.stream().map(assignment -> {
            ShipmentRouteCourierDTO dto = shipmentRouteCourierMapper.toDTO(assignment);
            AssignmentProgress progress = progressById.get(assignment.getId());
            dto.setPickedUpForDelivery(progress != null && progress.getPickedUpAt() != null);
            dto.setFailed(progress != null && progress.getFailedAt() != null);
            return dto;
        }).toList();
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
        return courierService.findIdByEmail(VerifiedEmails.require(authentication));
    }
}

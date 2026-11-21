package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRouteCourierService {

    private final ApplicationEventPublisher eventPublisher;
    private final KafkaProducerService kafkaProducerService;
    private final ShipmentRouteCourierMapper shipmentRouteCourierMapper;
    private final ShipmentRouteCourierRepository shipmentRouteCourierRepository;

    public long countByCourierIdAndDate(Long courierId, LocalDate date) {
        return shipmentRouteCourierRepository.countByCourierIdAndDate(courierId, date);
    }

    public List<ShipmentRouteCourierDTO> findByCourierIdAndDateAssignedFor(Long courierId, LocalDate date) {
        return shipmentRouteCourierRepository.findByCourierIdAndDateAssignedFor(courierId, date).stream()
                .map(shipmentRouteCourierMapper::toDTO)
                .toList();
    }

    @Transactional
    public ShipmentRouteCourierDTO save(ShipmentRouteCourierDTO dto) {
        ShipmentRouteCourier saved = shipmentRouteCourierRepository.save(shipmentRouteCourierMapper.toEntity(dto));
        ShipmentRouteCourierDTO savedDTO = shipmentRouteCourierMapper.toDTO(saved);
        kafkaProducerService.sendShipmentRouteCourier(savedDTO);
        return savedDTO;
    }

    /** Applies a status reported by courier and republishes the assignment; repeated events are ignored. */
    @Transactional
    public void applyStatus(AssignmentStatusEventDTO event) {
        Optional<ShipmentRouteCourier> found = shipmentRouteCourierRepository.findById(event.getAssignmentId());
        if (found.isEmpty()) {
            log.warn("Ignoring {} event for unknown assignment {}", event.getStatus(), event.getAssignmentId());
            return;
        }
        ShipmentRouteCourier assignment = found.get();
        switch (event.getStatus()) {
            case PICKED_UP -> {
                if (Boolean.TRUE.equals(assignment.getPickedUpForDelivery())) {
                    return;
                }
                assignment.setPickedUpForDelivery(true);
            }
            case FAILED -> {
                if (Boolean.TRUE.equals(assignment.getFailed())) {
                    return;
                }
                assignment.setFailed(true);
            }
        }
        // Sent to Kafka by KafkaProducerService only after this transaction commits.
        eventPublisher.publishEvent(shipmentRouteCourierMapper.toDTO(shipmentRouteCourierRepository.save(assignment)));
    }
}

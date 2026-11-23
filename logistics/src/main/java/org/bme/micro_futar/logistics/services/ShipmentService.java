package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.dtos.ShipmentManualReviewDTO;
import org.bme.micro_futar.logistics.entities.Shipment;
import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.logistics.entities.ShipmentPricing;
import org.bme.micro_futar.logistics.mappers.ShipmentMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentPricingRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.services.planners.ShipmentRoutePlanner;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentMapper shipmentMapper;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentRouteRepository shipmentRouteRepository;
    private final ShipmentRouteCourierRepository shipmentRouteCourierRepository;
    private final ShipmentManualReviewRepository shipmentManualReviewRepository;
    private final ShipmentRouteMapper shipmentRouteMapper;
    private final ShipmentRouteCourierMapper shipmentRouteCourierMapper;
    private final ShipmentPricingRepository shipmentPricingRepository;
    private final ShipmentRoutePlanner shipmentRoutePlanner;
    private final ShipmentPricingService shipmentPricingService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processShipment(ShipmentDTO shipmentDTO) {
        shipmentRepository.save(shipmentMapper.toEntity(shipmentDTO));

        if (shipmentDTO.isCanceled()) {
            cancelShipment(shipmentDTO);
            return;
        }

        if (!shipmentDTO.isConfirmed() || shipmentRouteRepository.existsByShipmentId(shipmentDTO.getId())) {
            return;
        }

        double routePrice = shipmentRoutePlanner.planRouteForShipment(shipmentDTO);

        // Shipments from before online payment have no authorized maximum, so there is nothing to capture.
        if (shipmentDTO.getMaxPrice() != null) {
            BigDecimal finalPrice = shipmentPricingService.finalPrice(shipmentDTO, routePrice);
            ZonedDateTime pricedAt = ZonedDateTime.now();
            ShipmentPricing pricing = new ShipmentPricing();
            pricing.setShipmentId(shipmentDTO.getId());
            pricing.setFinalPrice(finalPrice);
            pricing.setCurrencyCode(shipmentDTO.getCurrencyCode());
            pricing.setPricedAt(pricedAt);
            shipmentPricingRepository.save(pricing);
            // Sent to Kafka by KafkaProducerService only after this transaction commits.
            eventPublisher.publishEvent(ShipmentPricedEventDTO.builder()
                    .shipmentId(shipmentDTO.getId())
                    .finalAmount(finalPrice)
                    .currencyCode(shipmentDTO.getCurrencyCode())
                    .pricedAt(pricedAt)
                    .build());
        }
    }

    /**
     * Cancels what logistics planned for a shipment whose payment fell through. Nothing is planned if there is no
     * route yet. Routes and assignments are marked canceled (never deleted) and republished; a shipment that a
     * courier already picked up or that is delivered is left alone and flagged for manual handling instead.
     * Repeated messages change and publish nothing.
     */
    private void cancelShipment(ShipmentDTO shipmentDTO) {
        List<ShipmentRoute> routes = shipmentRouteRepository.findByShipmentId(shipmentDTO.getId());
        if (routes.isEmpty()) {
            return;
        }
        List<ShipmentRouteCourier> assignments = shipmentRouteCourierRepository.findByShipmentRouteIdIn(
                routes.stream().map(ShipmentRoute::getId).toList());

        boolean pickedUp = assignments.stream()
                .anyMatch(assignment -> Boolean.TRUE.equals(assignment.getPickedUpForDelivery()));
        if (shipmentDTO.isDelivered() || pickedUp) {
            flagForManualReview(shipmentDTO, shipmentDTO.isDelivered()
                    ? "Shipment was canceled after it had been delivered"
                    : "Shipment was canceled after a courier had picked it up");
            return;
        }

        Set<Long> routeIdsToCancel = new HashSet<>();
        for (ShipmentRoute route : routes) {
            if (route.getFulfillmentTime() == null && !route.isCanceled()) {
                route.setCanceled(true);
                routeIdsToCancel.add(route.getId());
                // Sent to Kafka by KafkaProducerService only after this transaction commits.
                eventPublisher.publishEvent(shipmentRouteMapper.toDTO(shipmentRouteRepository.save(route)));
            }
        }
        for (ShipmentRouteCourier assignment : assignments) {
            if (!assignment.isCanceled() && routeIdsToCancel.contains(assignment.getShipmentRouteId())) {
                assignment.setCanceled(true);
                eventPublisher.publishEvent(
                        shipmentRouteCourierMapper.toDTO(shipmentRouteCourierRepository.save(assignment)));
            }
        }
    }

    private void flagForManualReview(ShipmentDTO shipmentDTO, String reason) {
        log.warn("Shipment {} needs manual handling: {}", shipmentDTO.getId(), reason);
        if (shipmentManualReviewRepository.existsById(shipmentDTO.getId())) {
            return;
        }
        ShipmentManualReview review = new ShipmentManualReview();
        review.setShipmentId(shipmentDTO.getId());
        review.setReason(reason);
        review.setFlaggedAt(ZonedDateTime.now());
        shipmentManualReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<ShipmentManualReviewDTO> getManualReviews() {
        return shipmentManualReviewRepository.findAllByOrderByFlaggedAtDesc().stream()
                .map(review -> {
                    Shipment shipment = shipmentRepository.findById(review.getShipmentId()).orElse(null);
                    return ShipmentManualReviewDTO.builder()
                            .shipmentId(review.getShipmentId())
                            .parcelNumber(shipment != null ? shipment.getParcelNumber() : null)
                            .recipientAddress(shipment != null ? shipment.getRecipientAddress() : null)
                            .delivered(shipment != null && shipment.isDelivered())
                            .reason(review.getReason())
                            .flaggedAt(review.getFlaggedAt())
                            .build();
                })
                .toList();
    }

    public ShipmentDTO getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .map(shipmentMapper::toDTO)
                .orElse(null);
    }
}

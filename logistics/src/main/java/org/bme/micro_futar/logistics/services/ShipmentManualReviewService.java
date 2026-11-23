package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/** Puts shipments that cannot be handled automatically on the list an administrator works through. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentManualReviewService {

    private final ShipmentManualReviewRepository shipmentManualReviewRepository;

    /** Flags the shipment for manual handling; a shipment already on the list keeps its first reason. */
    @Transactional
    public void flag(Long shipmentId, String reason) {
        log.warn("Shipment {} needs manual handling: {}", shipmentId, reason);
        if (shipmentManualReviewRepository.existsById(shipmentId)) {
            return;
        }
        ShipmentManualReview review = new ShipmentManualReview();
        review.setShipmentId(shipmentId);
        review.setReason(reason);
        review.setFlaggedAt(ZonedDateTime.now());
        shipmentManualReviewRepository.save(review);
    }
}

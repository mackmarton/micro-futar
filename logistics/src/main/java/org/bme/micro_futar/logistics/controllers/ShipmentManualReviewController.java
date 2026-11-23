package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.ShipmentManualReviewDTO;
import org.bme.micro_futar.logistics.services.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipment-manual-reviews")
@RequiredArgsConstructor
public class ShipmentManualReviewController {

    private final ShipmentService shipmentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShipmentManualReviewDTO>> getManualReviews() {
        return ResponseEntity.ok(shipmentService.getManualReviews());
    }
}

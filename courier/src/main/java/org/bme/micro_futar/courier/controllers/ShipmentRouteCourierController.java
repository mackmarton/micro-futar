package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.ShipmentRouteCourierService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/shipment-route-couriers")
@RequiredArgsConstructor
public class ShipmentRouteCourierController {

    private final ShipmentRouteCourierService shipmentRouteCourierService;

    @GetMapping
    public ResponseEntity<List<ShipmentRouteCourierDTO>> findAll() {
        log.debug("Finding all shipmentRouteCouriers");
        List<ShipmentRouteCourierDTO> shipmentRouteCouriers = shipmentRouteCourierService.findAll();
        return ResponseEntity.ok(shipmentRouteCouriers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentRouteCourierDTO> findById(@PathVariable Long id) {
        log.debug("Finding shipmentRouteCourier by id: {}", id);
        return shipmentRouteCourierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pickup")
    public ResponseEntity<Void> pickUpShipmentForDelivery(@PathVariable Long id) {
        return shipmentRouteCourierService.pickUpShipmentForDelivery(id) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/fulfill")
    public ResponseEntity<Void> fulfillShipmentRouteAssignment(@PathVariable Long id) {
        return shipmentRouteCourierService.fulfillAssignment(id) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<Void> failShipmentRouteAssignment(@PathVariable Long id) {
        return shipmentRouteCourierService.failAssignment(id) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }
}


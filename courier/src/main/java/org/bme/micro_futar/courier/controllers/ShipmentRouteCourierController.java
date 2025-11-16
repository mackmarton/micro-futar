package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.ShipmentRouteCourierService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}


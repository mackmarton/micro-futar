package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("new")
    public ResponseEntity<ShipmentDTO> newShipment(@RequestBody ShipmentDTO shipmentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.newShipment(shipmentDTO));
    }
}

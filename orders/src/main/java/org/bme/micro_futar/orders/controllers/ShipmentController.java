package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.dtos.CreateShipmentResponseDTO;
import org.bme.micro_futar.orders.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.exceptions.EmailNotVerifiedException;
import org.bme.micro_futar.shared.utils.VerifiedEmail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("new")
    public ResponseEntity<CreateShipmentResponseDTO> newShipment(@RequestBody ShipmentDTO shipmentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.newShipment(shipmentDTO));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getShipmentsForUser(Authentication authentication){
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = VerifiedEmail.from(jwt.getClaims()).orElseThrow(EmailNotVerifiedException::new);
        return ResponseEntity.ok(shipmentService.getShipmentsForUser(userEmail));
    }

    @PostMapping("republish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> republishShipments() {
        int count = shipmentService.republishAllShipments();
        return ResponseEntity.ok(Map.of("status", "success", "message", count + " shipments sent to Kafka"));
    }
}

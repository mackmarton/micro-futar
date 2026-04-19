package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("new")
    public ResponseEntity<ShipmentDTO> newShipment(@RequestBody ShipmentDTO shipmentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.newShipment(shipmentDTO));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getShipmentsForUser(Authentication authentication){
        String userEmail = extractUserEmail(authentication);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(shipmentService.getShipmentsForUser(userEmail));
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

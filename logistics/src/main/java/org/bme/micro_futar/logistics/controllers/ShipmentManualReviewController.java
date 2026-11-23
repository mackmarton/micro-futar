package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.ShipmentManualReviewDTO;
import org.bme.micro_futar.logistics.services.ShipmentManualReviewService;
import org.bme.micro_futar.logistics.services.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipment-manual-reviews")
@RequiredArgsConstructor
public class ShipmentManualReviewController {

    private final ShipmentService shipmentService;
    private final ShipmentManualReviewService shipmentManualReviewService;

    /** Open items by default; {@code includeResolved=true} adds the ones an administrator already closed. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShipmentManualReviewDTO>> getManualReviews(
            @RequestParam(defaultValue = "false") boolean includeResolved) {
        return ResponseEntity.ok(shipmentService.getManualReviews(includeResolved));
    }

    @PostMapping("/{shipmentId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resolve(@PathVariable Long shipmentId, Authentication authentication) {
        return shipmentManualReviewService.resolve(shipmentId, resolvedBy(authentication))
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private static String resolvedBy(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwt) {
            String email = jwt.getToken().getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        return authentication != null ? authentication.getName() : null;
    }
}

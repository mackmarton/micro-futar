package org.bme.micro_futar.payments.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.payments.dtos.CheckoutRequestDTO;
import org.bme.micro_futar.payments.dtos.CheckoutResponseDTO;
import org.bme.micro_futar.payments.dtos.PaymentStatusResponseDTO;
import org.bme.micro_futar.payments.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/**
 * Orders can be placed without an account, so these endpoints are public. Ownership is proven with the payment
 * token orders returned when the shipment was created (never put in the URL), or by a logged-in sender's email.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api")
public class PaymentController {

    public static final String PAYMENT_TOKEN_HEADER = "X-Payment-Token";

    private final PaymentService paymentService;

    @PostMapping("checkout")
    public ResponseEntity<CheckoutResponseDTO> startCheckout(@RequestBody CheckoutRequestDTO request,
                                                             Authentication authentication) {
        return ResponseEntity.ok(paymentService.startCheckout(request.getShipmentId(), request.getPaymentToken(),
                extractUserEmail(authentication), request.getLocale()));
    }

    @GetMapping("status/{shipmentId}")
    public ResponseEntity<PaymentStatusResponseDTO> getStatus(
            @PathVariable Long shipmentId,
            @RequestHeader(value = PAYMENT_TOKEN_HEADER, required = false) String paymentToken,
            Authentication authentication) {
        return ResponseEntity.ok(paymentService.getStatus(shipmentId, paymentToken, extractUserEmail(authentication)));
    }

    private static String extractUserEmail(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String email = jwtAuthenticationToken.getToken().getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        return null;
    }
}

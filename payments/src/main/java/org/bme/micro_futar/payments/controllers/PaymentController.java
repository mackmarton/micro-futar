package org.bme.micro_futar.payments.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.payments.dtos.CheckoutRequestDTO;
import org.bme.micro_futar.payments.dtos.CheckoutResponseDTO;
import org.bme.micro_futar.payments.dtos.PaymentStatusResponseDTO;
import org.bme.micro_futar.payments.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Orders can be placed without an account, so these endpoints are public and ownership is proven with the
 * payment token orders returned when the shipment was created. The token is never put in the URL.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api")
public class PaymentController {

    public static final String PAYMENT_TOKEN_HEADER = "X-Payment-Token";

    private final PaymentService paymentService;

    @PostMapping("checkout")
    public ResponseEntity<CheckoutResponseDTO> startCheckout(@RequestBody CheckoutRequestDTO request) {
        return ResponseEntity.ok(paymentService.startCheckout(request.getShipmentId(), request.getPaymentToken()));
    }

    @GetMapping("status/{shipmentId}")
    public ResponseEntity<PaymentStatusResponseDTO> getStatus(@PathVariable Long shipmentId,
                                                              @RequestHeader(PAYMENT_TOKEN_HEADER) String paymentToken) {
        return ResponseEntity.ok(paymentService.getStatus(shipmentId, paymentToken));
    }
}

package org.bme.micro_futar.payments.controllers;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.payments.services.StripeWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Called by Stripe, not by our UI: authenticated by the Stripe-Signature header instead of a JWT. */
@Hidden
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/webhooks")
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @PostMapping("stripe")
    public ResponseEntity<Void> handleStripeEvent(@RequestBody String payload,
                                                  @RequestHeader("Stripe-Signature") String signature) {
        Event event;
        try {
            event = stripeWebhookService.verify(payload, signature);
        } catch (SignatureVerificationException e) {
            log.warn("Rejected Stripe webhook with invalid signature");
            return ResponseEntity.badRequest().build();
        }
        try {
            stripeWebhookService.handle(event);
        } catch (Exception e) {
            // Non-2xx makes Stripe retry the event with backoff.
            log.error("Failed to handle Stripe event {} ({})", event.getId(), event.getType(), e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}

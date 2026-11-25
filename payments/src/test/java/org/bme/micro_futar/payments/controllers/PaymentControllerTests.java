package org.bme.micro_futar.payments.controllers;

import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.exceptions.PaymentNotFoundException;
import org.bme.micro_futar.payments.repositories.PaymentRepository;
import org.bme.micro_futar.payments.services.PaymentService;
import org.bme.micro_futar.payments.services.PaymentTokenHasher;
import org.bme.micro_futar.payments.services.StripeGateway;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTests {

    private static final Long SHIPMENT_ID = 42L;
    private static final String TOKEN = "token-of-the-sender";

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private StripeGateway stripeGateway;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaymentController controller;

    @BeforeEach
    void setUp() {
        PaymentTokenHasher hasher = new PaymentTokenHasher();
        controller = new PaymentController(
                new PaymentService(paymentRepository, hasher, stripeGateway, eventPublisher));
        Payment payment = new Payment();
        payment.setShipmentId(SHIPMENT_ID);
        payment.setSenderEmail("john@example.com");
        payment.setPaymentTokenHash(hasher.hash(TOKEN));
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));
    }

    private static Authentication sender(boolean emailVerified) {
        return new JwtAuthenticationToken(Jwt.withTokenValue("t").header("alg", "none")
                .claim("email", "john@example.com")
                .claim("email_verified", emailVerified)
                .build());
    }

    @Test
    void verifiedEmailProvesOwnership() {
        assertThat(controller.getStatus(SHIPMENT_ID, null, sender(true)).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void unverifiedEmailDoesNotProveOwnership() {
        Authentication unverified = sender(false);

        assertThatThrownBy(() -> controller.getStatus(SHIPMENT_ID, null, unverified))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void unverifiedSenderCanStillUseThePaymentToken() {
        assertThat(controller.getStatus(SHIPMENT_ID, TOKEN, sender(false)).getStatusCode().value()).isEqualTo(200);
    }
}

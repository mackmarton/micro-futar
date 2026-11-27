package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.mappers.ShipmentMapper;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** The payment state transitions that the integration tests do not walk through. */
@ExtendWith(MockitoExtension.class)
class ShipmentServiceTests {

    private static final long ID = 1L;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private ShipmentMapper shipmentMapper = Mappers.getMapper(ShipmentMapper.class);

    @InjectMocks
    private ShipmentService shipmentService;

    private Shipment shipment;

    @BeforeEach
    void setUp() {
        shipment = TestFixtures.pendingShipment();
        shipment.setId(ID);
        when(shipmentRepository.findById(ID)).thenReturn(Optional.of(shipment));
        // not every transition is saved
        lenient().when(shipmentRepository.save(shipment)).thenReturn(shipment);
    }

    @Test
    void lateAuthorizationDoesNotReopenAPaidShipment() {
        shipment.setPaymentStatus(PaymentStatus.PAID);

        apply(PaymentStatus.AUTHORIZED);

        assertThat(shipment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(shipment.isConfirmed()).isFalse();
        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"EXPIRED", "CANCELED"})
    void paymentFailureAfterAuthorizationCancelsAndRepublishesTheShipment(PaymentStatus failure) {
        givenAuthorizedShipment();

        apply(failure);

        assertThat(shipment.getPaymentStatus()).isEqualTo(failure);
        assertThat(shipment.isConfirmed()).isFalse();
        assertThat(shipment.isCanceled()).isTrue();
        verify(eventPublisher).publishEvent(any(ShipmentDTO.class));
    }

    @Test
    void furtherFailuresAfterCancellationAreNotRepublished() {
        givenAuthorizedShipment();

        apply(PaymentStatus.FAILED);
        apply(PaymentStatus.FAILED);
        apply(PaymentStatus.EXPIRED);

        assertThat(shipment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(shipment.isCanceled()).isTrue();
        verify(eventPublisher).publishEvent(any(ShipmentDTO.class));
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"PAID", "REFUNDED"})
    void paymentFailureDoesNotOverwriteASettledPayment(PaymentStatus settled) {
        shipment.setPaymentStatus(settled);

        apply(PaymentStatus.CANCELED);

        assertThat(shipment.getPaymentStatus()).isEqualTo(settled);
        assertThat(shipment.isCanceled()).isFalse();
        verifyNoInteractions(eventPublisher);
    }

    private void givenAuthorizedShipment() {
        shipment.setPaymentStatus(PaymentStatus.AUTHORIZED);
        shipment.setConfirmed(true);
    }

    private void apply(PaymentStatus status) {
        shipmentService.applyPaymentStatus(PaymentStatusEventDTO.of(ID, status, null, "HUF"));
    }
}

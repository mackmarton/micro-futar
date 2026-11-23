package org.bme.micro_futar.courier.services;

import org.bme.micro_futar.courier.entities.AssignmentProgress;
import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.courier.repositories.AssignmentProgressRepository;
import org.bme.micro_futar.courier.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.enums.AssignmentStatus;
import org.bme.micro_futar.shared.exceptions.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShipmentRouteCourierServiceTests {

    @Mock
    private CourierService courierService;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private ShipmentRouteService shipmentRouteService;
    @Mock
    private ShipmentNotificationService shipmentNotificationService;
    @Mock
    private ShipmentRouteCourierMapper shipmentRouteCourierMapper;
    @Mock
    private ShipmentRouteCourierRepository shipmentRouteCourierRepository;
    @Mock
    private AssignmentProgressRepository assignmentProgressRepository;

    @InjectMocks
    private ShipmentRouteCourierService service;

    private final Map<Long, AssignmentProgress> progressStore = new HashMap<>();
    private Authentication authentication;
    private ShipmentRouteCourier entity;

    @BeforeEach
    void setUp() {
        authentication = new JwtAuthenticationToken(
                Jwt.withTokenValue("t").header("alg", "none").claim("email", "courier@example.com").build());
        entity = new ShipmentRouteCourier();
        entity.setId(5L);
        entity.setCourierId(2L);
        entity.setShipmentRouteId(3L);
        ShipmentRouteCourierDTO dto = new ShipmentRouteCourierDTO();
        dto.setId(5L);
        dto.setCourierId(2L);
        dto.setShipmentRouteId(3L);
        lenient().when(courierService.findIdByEmail("courier@example.com")).thenReturn(2L);
        lenient().when(shipmentRouteCourierRepository.findById(5L)).thenReturn(Optional.of(entity));
        lenient().when(shipmentRouteCourierMapper.toDTO(entity)).thenAnswer(invocation -> {
            ShipmentRouteCourierDTO copy = new ShipmentRouteCourierDTO();
            copy.setId(5L);
            copy.setCourierId(2L);
            copy.setShipmentRouteId(3L);
            copy.setCanceled(entity.isCanceled());
            return copy;
        });
        lenient().when(assignmentProgressRepository.findById(anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(progressStore.get(invocation.<Long>getArgument(0))));
        lenient().when(assignmentProgressRepository.findAllById(any()))
                .thenAnswer(invocation -> progressStore.values().stream().toList());
        lenient().when(assignmentProgressRepository.save(any(AssignmentProgress.class)))
                .thenAnswer(invocation -> {
                    AssignmentProgress progress = invocation.getArgument(0);
                    progressStore.put(progress.getAssignmentId(), progress);
                    return progress;
                });
    }

    @Test
    void pickingUpRecordsProgressAndPublishesStatusOnlyOnce() {
        service.pickUpParcel(5L, authentication);
        service.pickUpParcel(5L, authentication);

        assertThat(progressStore.get(5L).getPickedUpAt()).isNotNull();
        verify(kafkaProducerService, times(1)).sendAssignmentStatus(argThat((AssignmentStatusEventDTO event) ->
                event.getAssignmentId().equals(5L) && event.getShipmentRouteId().equals(3L)
                        && event.getStatus() == AssignmentStatus.PICKED_UP));
    }

    @Test
    void pickedUpStateIsVisibleImmediatelyWithoutWaitingForLogistics() {
        service.pickUpParcel(5L, authentication);

        assertThat(service.findById(5L)).get()
                .satisfies(dto -> assertThat(dto.getPickedUpForDelivery()).isTrue());
    }

    @Test
    void failingRecordsProgressAndPublishesStatusOnlyOnce() {
        service.failAssignment(5L, authentication);
        service.failAssignment(5L, authentication);

        assertThat(progressStore.get(5L).getFailedAt()).isNotNull();
        verify(kafkaProducerService, times(1)).sendAssignmentStatus(argThat((AssignmentStatusEventDTO event) ->
                event.getStatus() == AssignmentStatus.FAILED));
    }

    @Test
    void fulfillingTwiceReportsTheLegOnlyOnce() {
        service.fulfillAssignment(5L, authentication);
        service.fulfillAssignment(5L, authentication);

        verify(shipmentRouteService, times(1)).fulfillShipmentRoute(anyLong(), any(ZonedDateTime.class));
        verify(shipmentRouteService).fulfillShipmentRoute(org.mockito.ArgumentMatchers.eq(3L), any(ZonedDateTime.class));
    }

    @Test
    void canceledAssignmentThatWasNeverPickedUpCannotBePickedUpFulfilledOrFailed() {
        entity.setCanceled(true);

        service.pickUpParcel(5L, authentication);
        service.fulfillAssignment(5L, authentication);
        service.failAssignment(5L, authentication);

        assertThat(progressStore).isEmpty();
        verify(kafkaProducerService, never()).sendAssignmentStatus(any());
        verify(shipmentRouteService, never()).fulfillShipmentRoute(anyLong(), any(ZonedDateTime.class));
    }

    @Test
    void canceledAssignmentAlreadyPickedUpCanStillBeFulfilledBecauseTheParcelIsWithTheCourier() {
        service.pickUpParcel(5L, authentication);
        entity.setCanceled(true);

        service.fulfillAssignment(5L, authentication);
        service.fulfillAssignment(5L, authentication);

        assertThat(progressStore.get(5L).getFulfilledAt()).isNotNull();
        verify(shipmentRouteService, times(1)).fulfillShipmentRoute(org.mockito.ArgumentMatchers.eq(3L),
                any(ZonedDateTime.class));
    }

    @Test
    void canceledAssignmentAlreadyPickedUpCanStillBeFailed() {
        service.pickUpParcel(5L, authentication);
        entity.setCanceled(true);

        service.failAssignment(5L, authentication);
        service.failAssignment(5L, authentication);

        assertThat(progressStore.get(5L).getFailedAt()).isNotNull();
        verify(kafkaProducerService, times(1)).sendAssignmentStatus(argThat((AssignmentStatusEventDTO event) ->
                event.getStatus() == AssignmentStatus.FAILED));
    }

    @Test
    void upsertFromLogisticsNeverTouchesCourierProgress() {
        ShipmentRouteCourierDTO incoming = new ShipmentRouteCourierDTO();
        incoming.setId(5L);
        org.mockito.Mockito.when(shipmentRouteCourierMapper.toEntity(incoming)).thenReturn(entity);
        org.mockito.Mockito.when(shipmentRouteCourierRepository.save(entity)).thenReturn(entity);

        service.saveWithoutTopicSend(incoming);

        verify(assignmentProgressRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendAssignmentStatus(any());
        assertThat(List.copyOf(progressStore.values())).isEmpty();
    }

    @Test
    void failedEventSendPropagatesSoTheTransactionRollsBackAndNothingIsNotified() {
        org.mockito.Mockito.doThrow(new KafkaException("broker down"))
                .when(kafkaProducerService).sendAssignmentStatus(any());

        assertThatThrownBy(() -> service.failAssignment(5L, authentication)).isInstanceOf(KafkaException.class);

        verify(shipmentNotificationService, never()).notifyDeliveryFailed(any(), anyLong());
    }

    @Test
    void failedFulfilledEventSendPropagates() {
        org.mockito.Mockito.doThrow(new KafkaException("broker down"))
                .when(shipmentRouteService).fulfillShipmentRoute(anyLong(), any(ZonedDateTime.class));

        assertThatThrownBy(() -> service.fulfillAssignment(5L, authentication)).isInstanceOf(KafkaException.class);
    }

    @Test
    void actionsThatPublishFactsRunInATransaction() {
        for (String name : List.of("pickUpParcel", "pickUpAllDeliveryShipmentsForCurrentDay", "fulfillAssignment",
                "fulfillAllPickupsForCurrentDay", "failAssignment")) {
            assertThat(java.util.Arrays.stream(ShipmentRouteCourierService.class.getDeclaredMethods())
                    .filter(m -> m.getName().equals(name))
                    .allMatch(m -> m.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class)))
                    .as(name).isTrue();
        }
    }
}

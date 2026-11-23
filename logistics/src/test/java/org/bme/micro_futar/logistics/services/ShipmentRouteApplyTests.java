package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
import org.bme.micro_futar.shared.enums.AssignmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Logistics owns routes and assignments; courier only reports facts, which are applied here. */
@ExtendWith(MockitoExtension.class)
class ShipmentRouteApplyTests {

    @Mock
    private ShipmentRouteRepository shipmentRouteRepository;
    @Mock
    private ShipmentRouteMapper shipmentRouteMapper;
    @Mock
    private ShipmentRouteCourierRepository shipmentRouteCourierRepository;
    @Mock
    private ShipmentRouteCourierMapper shipmentRouteCourierMapper;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ReferenceValidator referenceValidator;
    @Mock
    private ShipmentManualReviewService shipmentManualReviewService;

    private ShipmentRouteService routeService() {
        return new ShipmentRouteService(shipmentRouteRepository, shipmentRouteMapper, kafkaProducerService, eventPublisher,
                shipmentManualReviewService);
    }

    private ShipmentRouteCourierService assignmentService() {
        return new ShipmentRouteCourierService(eventPublisher, kafkaProducerService, shipmentRouteCourierMapper,
                shipmentRouteCourierRepository, referenceValidator, shipmentRouteRepository, shipmentManualReviewService);
    }

    @Test
    void fulfilledEventSetsFulfillmentTimeAndRepublishesTheRoute() {
        ZonedDateTime at = ZonedDateTime.now();
        ShipmentRoute route = new ShipmentRoute();
        route.setId(3L);
        ShipmentRouteDTO dto = new ShipmentRouteDTO();
        when(shipmentRouteRepository.findById(3L)).thenReturn(Optional.of(route));
        when(shipmentRouteRepository.save(route)).thenReturn(route);
        when(shipmentRouteMapper.toDTO(route)).thenReturn(dto);

        routeService().applyFulfilled(
                ShipmentRouteFulfilledEventDTO.builder().shipmentRouteId(3L).shipmentId(7L).fulfilledAt(at).build());

        assertThat(route.getFulfillmentTime()).isEqualTo(at);
        verify(eventPublisher).publishEvent((Object) dto);
    }

    @Test
    void repeatedFulfilledEventKeepsTheFirstTimeAndDoesNotRepublish() {
        ZonedDateTime first = ZonedDateTime.now().minusHours(1);
        ShipmentRoute route = new ShipmentRoute();
        route.setFulfillmentTime(first);
        when(shipmentRouteRepository.findById(3L)).thenReturn(Optional.of(route));

        routeService().applyFulfilled(ShipmentRouteFulfilledEventDTO.builder()
                .shipmentRouteId(3L).fulfilledAt(ZonedDateTime.now()).build());

        assertThat(route.getFulfillmentTime()).isEqualTo(first);
        verify(shipmentRouteRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void fulfilledEventForUnknownRouteIsIgnored() {
        when(shipmentRouteRepository.findById(9L)).thenReturn(Optional.empty());

        routeService().applyFulfilled(ShipmentRouteFulfilledEventDTO.builder().shipmentRouteId(9L).build());

        verify(shipmentRouteRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void pickedUpAndFailedEventsSetTheFlagsAndRepublishTheAssignmentOnce() {
        ShipmentRouteCourier assignment = new ShipmentRouteCourier();
        ShipmentRouteCourierDTO dto = new ShipmentRouteCourierDTO();
        when(shipmentRouteCourierRepository.findById(5L)).thenReturn(Optional.of(assignment));
        when(shipmentRouteCourierRepository.save(assignment)).thenReturn(assignment);
        when(shipmentRouteCourierMapper.toDTO(assignment)).thenReturn(dto);
        ShipmentRouteCourierService service = assignmentService();

        service.applyStatus(status(AssignmentStatus.PICKED_UP));
        service.applyStatus(status(AssignmentStatus.PICKED_UP));
        service.applyStatus(status(AssignmentStatus.FAILED));
        service.applyStatus(status(AssignmentStatus.FAILED));

        assertThat(assignment.getPickedUpForDelivery()).isTrue();
        assertThat(assignment.getFailed()).isTrue();
        verify(eventPublisher, times(2)).publishEvent((Object) dto);
    }

    @Test
    void statusEventForUnknownAssignmentIsIgnored() {
        when(shipmentRouteCourierRepository.findById(5L)).thenReturn(Optional.empty());

        assignmentService().applyStatus(status(AssignmentStatus.PICKED_UP));

        verify(shipmentRouteCourierRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void pickedUpEventOnCanceledAssignmentIsRecordedPublishedAndFlaggedForManualReview() {
        ShipmentRouteCourier assignment = new ShipmentRouteCourier();
        assignment.setShipmentRouteId(3L);
        assignment.setCanceled(true);
        ShipmentRoute route = new ShipmentRoute();
        route.setShipmentId(7L);
        ShipmentRouteCourierDTO dto = new ShipmentRouteCourierDTO();
        when(shipmentRouteCourierRepository.findById(5L)).thenReturn(Optional.of(assignment));
        when(shipmentRouteCourierRepository.save(assignment)).thenReturn(assignment);
        when(shipmentRouteCourierMapper.toDTO(assignment)).thenReturn(dto);
        when(shipmentRouteRepository.findById(3L)).thenReturn(Optional.of(route));
        ShipmentRouteCourierService service = assignmentService();

        service.applyStatus(status(AssignmentStatus.PICKED_UP));
        service.applyStatus(status(AssignmentStatus.PICKED_UP));

        assertThat(assignment.getPickedUpForDelivery()).isTrue();
        verify(eventPublisher, times(1)).publishEvent((Object) dto);
        verify(shipmentManualReviewService, times(1)).flag(7L, "Picked up after cancellation");
    }

    @Test
    void failedEventOnCanceledAssignmentIsRecordedWithoutManualReview() {
        ShipmentRouteCourier assignment = new ShipmentRouteCourier();
        assignment.setCanceled(true);
        when(shipmentRouteCourierRepository.findById(5L)).thenReturn(Optional.of(assignment));
        when(shipmentRouteCourierRepository.save(assignment)).thenReturn(assignment);
        when(shipmentRouteCourierMapper.toDTO(assignment)).thenReturn(new ShipmentRouteCourierDTO());

        assignmentService().applyStatus(status(AssignmentStatus.FAILED));

        assertThat(assignment.getFailed()).isTrue();
        verifyNoInteractions(shipmentManualReviewService);
    }

    @Test
    void pickedUpEventOnRegularAssignmentIsNotFlagged() {
        ShipmentRouteCourier assignment = new ShipmentRouteCourier();
        when(shipmentRouteCourierRepository.findById(5L)).thenReturn(Optional.of(assignment));
        when(shipmentRouteCourierRepository.save(assignment)).thenReturn(assignment);
        when(shipmentRouteCourierMapper.toDTO(assignment)).thenReturn(new ShipmentRouteCourierDTO());

        assignmentService().applyStatus(status(AssignmentStatus.PICKED_UP));

        verifyNoInteractions(shipmentManualReviewService);
    }

    @Test
    void fulfilledEventOnCanceledRouteIsRecordedPublishedAndFlaggedForManualReview() {
        ZonedDateTime at = ZonedDateTime.now();
        ShipmentRoute route = new ShipmentRoute();
        route.setId(3L);
        route.setShipmentId(7L);
        route.setCanceled(true);
        ShipmentRouteDTO dto = new ShipmentRouteDTO();
        when(shipmentRouteRepository.findById(3L)).thenReturn(Optional.of(route));
        when(shipmentRouteRepository.save(route)).thenReturn(route);
        when(shipmentRouteMapper.toDTO(route)).thenReturn(dto);

        ShipmentRouteFulfilledEventDTO event =
                ShipmentRouteFulfilledEventDTO.builder().shipmentRouteId(3L).shipmentId(7L).fulfilledAt(at).build();
        routeService().applyFulfilled(event);
        routeService().applyFulfilled(event);

        assertThat(route.getFulfillmentTime()).isEqualTo(at);
        verify(eventPublisher, times(1)).publishEvent((Object) dto);
        verify(shipmentManualReviewService, times(1)).flag(7L, "Leg fulfilled after cancellation");
    }

    @Test
    void fulfilledEventOnRegularRouteIsNotFlagged() {
        ShipmentRoute route = new ShipmentRoute();
        when(shipmentRouteRepository.findById(3L)).thenReturn(Optional.of(route));
        when(shipmentRouteRepository.save(route)).thenReturn(route);
        when(shipmentRouteMapper.toDTO(route)).thenReturn(new ShipmentRouteDTO());

        routeService().applyFulfilled(
                ShipmentRouteFulfilledEventDTO.builder().shipmentRouteId(3L).fulfilledAt(ZonedDateTime.now()).build());

        verifyNoInteractions(shipmentManualReviewService);
    }

    @Test
    void logisticsNoLongerSavesRoutesOrAssignmentsReceivedFromTopics() {
        assertThat(Arrays.stream(ShipmentRouteService.class.getDeclaredMethods()).map(m -> m.getName()))
                .doesNotContain("saveWithoutTopicSend");
        assertThat(Arrays.stream(ShipmentRouteCourierService.class.getDeclaredMethods()).map(m -> m.getName()))
                .doesNotContain("saveWithoutTopicSend");
    }

    private static AssignmentStatusEventDTO status(AssignmentStatus status) {
        return AssignmentStatusEventDTO.builder().assignmentId(5L).shipmentRouteId(3L).status(status)
                .occurredAt(ZonedDateTime.now()).build();
    }
}

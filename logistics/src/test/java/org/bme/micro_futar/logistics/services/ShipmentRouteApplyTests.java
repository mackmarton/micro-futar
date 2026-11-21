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

    private ShipmentRouteService routeService() {
        return new ShipmentRouteService(shipmentRouteRepository, shipmentRouteMapper, kafkaProducerService, eventPublisher);
    }

    private ShipmentRouteCourierService assignmentService() {
        return new ShipmentRouteCourierService(eventPublisher, kafkaProducerService, shipmentRouteCourierMapper,
                shipmentRouteCourierRepository);
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

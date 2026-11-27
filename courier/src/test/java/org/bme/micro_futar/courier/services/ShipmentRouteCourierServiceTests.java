package org.bme.micro_futar.courier.services;

import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.CourierMapper;
import org.bme.micro_futar.courier.mappers.ShipmentMapper;
import org.bme.micro_futar.courier.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.courier.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.courier.repositories.AssignmentProgressRepository;
import org.bme.micro_futar.courier.repositories.CourierRepository;
import org.bme.micro_futar.courier.repositories.ShipmentRepository;
import org.bme.micro_futar.courier.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.courier.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.shared.exceptions.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** A fact event the broker does not acknowledge must reach the transaction; no integration test can break the broker. */
@ExtendWith(MockitoExtension.class)
class ShipmentRouteCourierServiceTests {

    private static final long ASSIGNMENT = 1L;

    @Mock private KafkaProducerService kafkaProducerService;
    @Mock private CourierRepository courierRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private ShipmentRouteRepository shipmentRouteRepository;
    @Mock private ShipmentRouteCourierRepository shipmentRouteCourierRepository;
    @Mock private AssignmentProgressRepository assignmentProgressRepository;

    private ShipmentRouteCourierService service;

    private final Authentication courier = new JwtAuthenticationToken(Jwt.withTokenValue("t")
            .header("alg", "none")
            .claim("email", TestFixtures.COURIER_EMAIL)
            .claim("email_verified", true)
            .build());

    @BeforeEach
    void setUp() {
        var notifications = new ShipmentNotificationService(
                new ShipmentService(Mappers.getMapper(ShipmentMapper.class), shipmentRepository),
                kafkaProducerService);
        var routes = new ShipmentRouteService(notifications, Mappers.getMapper(ShipmentRouteMapper.class),
                kafkaProducerService, shipmentRouteRepository);
        var assignmentMapper = Mappers.getMapper(ShipmentRouteCourierMapper.class);
        service = new ShipmentRouteCourierService(
                new CourierService(courierRepository, Mappers.getMapper(CourierMapper.class)),
                assignmentProgressRepository, kafkaProducerService, routes, notifications, assignmentMapper,
                shipmentRouteCourierRepository);
    }

    @Test
    void failedStatusEventPropagatesAndNothingIsNotified() {
        givenOwnDeliveryAssignment();
        doThrow(new KafkaException("broker down")).when(kafkaProducerService).sendAssignmentStatus(any());

        assertThatThrownBy(() -> service.failAssignment(ASSIGNMENT, courier)).isInstanceOf(KafkaException.class);

        verify(kafkaProducerService, never()).sendShipmentNotification(any());
    }

    @Test
    void failedFulfilledEventPropagatesAndNothingElseIsPublished() {
        givenOwnDeliveryAssignment();
        when(shipmentRouteRepository.findById(TestFixtures.DELIVERY_ROUTE_ID)).thenReturn(Optional.of(
                Mappers.getMapper(ShipmentRouteMapper.class).toEntity(TestFixtures.deliveryLeg().build())));
        doThrow(new KafkaException("broker down")).when(kafkaProducerService).sendShipmentRouteFulfilled(any());

        assertThatThrownBy(() -> service.fulfillAssignment(ASSIGNMENT, courier)).isInstanceOf(KafkaException.class);

        verify(kafkaProducerService, never()).sendShipmentDelivered(any());
        verify(kafkaProducerService, never()).sendShipmentNotification(any());
    }

    @Test
    void actionsThatPublishFactsRunInATransaction() {
        for (String name : List.of("pickUpParcel", "pickUpAllDeliveryShipmentsForCurrentDay", "fulfillAssignment",
                "fulfillAllPickupsForCurrentDay", "failAssignment")) {
            assertThat(Arrays.stream(ShipmentRouteCourierService.class.getDeclaredMethods())
                    .filter(method -> method.getName().equals(name))
                    .allMatch(method -> method.isAnnotationPresent(Transactional.class)))
                    .as(name).isTrue();
        }
    }

    private void givenOwnDeliveryAssignment() {
        var courierMapper = Mappers.getMapper(CourierMapper.class);
        when(courierRepository.findByEmail(TestFixtures.COURIER_EMAIL))
                .thenReturn(Optional.of(courierMapper.toEntity(TestFixtures.courier())));
        when(shipmentRouteCourierRepository.findById(ASSIGNMENT)).thenReturn(Optional.of(Mappers.getMapper(ShipmentRouteCourierMapper.class).toEntity(
                TestFixtures.assignment(ASSIGNMENT, TestFixtures.DELIVERY_ROUTE_ID).build())));
    }
}

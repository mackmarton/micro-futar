package org.bme.micro_futar.courier.services;

import org.bme.micro_futar.courier.entities.ShipmentRoute;
import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDeliveredEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentRouteServiceTests {

    @Mock
    private ShipmentNotificationService shipmentNotificationService;
    @Mock
    private ShipmentRouteMapper shipmentRouteMapper;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private ShipmentRouteRepository shipmentRouteRepository;

    @InjectMocks
    private ShipmentRouteService shipmentRouteService;

    private ShipmentRouteDTO routeDTO;

    @BeforeEach
    void setUp() {
        routeDTO = new ShipmentRouteDTO();
        routeDTO.setId(3L);
        routeDTO.setShipmentId(7L);
        ShipmentRoute entity = new ShipmentRoute();
        lenient().when(shipmentRouteRepository.findById(3L)).thenReturn(Optional.of(entity));
        lenient().when(shipmentRouteMapper.toDTO(entity)).thenReturn(routeDTO);
        lenient().when(shipmentRouteMapper.toEntity(routeDTO)).thenReturn(entity);
        lenient().when(shipmentRouteRepository.save(entity)).thenReturn(entity);
    }

    @Test
    void fulfillingTheDeliveryLegReportsFulfilledAndDeliveredAndNoRouteOrShipment() {
        routeDTO.setDestinationAddress("Test Avenue 2");
        ZonedDateTime at = ZonedDateTime.now();

        shipmentRouteService.fulfillShipmentRoute(3L, at);

        verify(kafkaProducerService).sendShipmentRouteFulfilled(argThat((ShipmentRouteFulfilledEventDTO event) ->
                event.getShipmentRouteId().equals(3L) && event.getShipmentId().equals(7L)
                        && event.getFulfilledAt().equals(at)));
        verify(kafkaProducerService).sendShipmentDelivered(argThat((ShipmentDeliveredEventDTO event) ->
                event.getShipmentId().equals(7L) && event.getDeliveredAt().equals(at)));
        verify(shipmentNotificationService).notifyDelivered(routeDTO);
    }

    @Test
    void fulfillingAnIntermediateLegDoesNotPublishDelivered() {
        routeDTO.setDestinationAddress(null);

        shipmentRouteService.fulfillShipmentRoute(3L, ZonedDateTime.now());

        verify(kafkaProducerService).sendShipmentRouteFulfilled(any());
        verify(kafkaProducerService, never()).sendShipmentDelivered(any());
        verify(shipmentNotificationService, never()).notifyDelivered(any());
    }

    @Test
    void courierDoesNotWriteToTopicsItDoesNotOwn() {
        assertThat(Arrays.stream(KafkaProducerService.class.getDeclaredMethods()).map(Method::getName))
                .doesNotContain("sendShipment", "sendShipmentRoute", "sendShipmentRouteCourier");
        assertThat(Arrays.stream(ShipmentService.class.getDeclaredMethods()).map(Method::getName))
                .doesNotContain("save");
        assertThat(Arrays.stream(ShipmentRouteService.class.getDeclaredMethods()).map(Method::getName))
                .doesNotContain("save");
    }
}

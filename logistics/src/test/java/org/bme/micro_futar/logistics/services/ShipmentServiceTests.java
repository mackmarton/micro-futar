package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.entities.Shipment;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import java.time.ZonedDateTime;
import java.util.List;
import org.bme.micro_futar.logistics.entities.ShipmentPricing;
import org.bme.micro_futar.logistics.mappers.ShipmentMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentPricingRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.services.planners.ShipmentRoutePlanner;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTests {

    @Mock
    private ShipmentMapper shipmentMapper;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentPricingRepository shipmentPricingRepository;

    @Mock
    private ShipmentRouteRepository shipmentRouteRepository;

    @Mock
    private ShipmentRouteCourierRepository shipmentRouteCourierRepository;

    @Mock
    private ShipmentManualReviewRepository shipmentManualReviewRepository;

    @Mock
    private ShipmentManualReviewService shipmentManualReviewService;

    @Mock
    private ShipmentRouteMapper shipmentRouteMapper;

    @Mock
    private ShipmentRouteCourierMapper shipmentRouteCourierMapper;

    @Mock
    private ShipmentRoutePlanner shipmentRoutePlanner;

    @Mock
    private ShipmentPricingService shipmentPricingService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShipmentService shipmentService;

    private ShipmentDTO shipmentDTO;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        shipmentDTO = new ShipmentDTO();
        shipmentDTO.setId(7L);
        shipmentDTO.setConfirmed(true);
        shipmentDTO.setCurrencyCode("HUF");
        shipment = new Shipment();
        lenient().when(shipmentMapper.toEntity(shipmentDTO)).thenReturn(shipment);
        lenient().when(shipmentRoutePlanner.planRouteForShipment(shipmentDTO)).thenReturn(2490.0);
    }

    @Test
    void paidShipmentIsPricedAfterRoutePlanningAndPriceIsStoredSeparately() {
        shipmentDTO.setMaxPrice(3000.0);
        when(shipmentPricingService.finalPrice(shipmentDTO, 2490.0)).thenReturn(new BigDecimal("2490.0"));

        shipmentService.processShipment(shipmentDTO);

        verify(shipmentRepository).save(shipment);
        verify(shipmentPricingRepository).save(argThat((ShipmentPricing pricing) ->
                pricing.getShipmentId().equals(7L)
                        && pricing.getFinalPrice().compareTo(new BigDecimal("2490")) == 0
                        && pricing.getCurrencyCode().equals("HUF")));
        verify(eventPublisher).publishEvent(argThat((ShipmentPricedEventDTO event) ->
                event.getShipmentId().equals(7L)
                        && event.getFinalAmount().compareTo(new BigDecimal("2490")) == 0
                        && event.getCurrencyCode().equals("HUF")));
    }

    @Test
    void shipmentWithoutPaymentIsNotPriced() {
        shipmentService.processShipment(shipmentDTO);

        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentRoutePlanner).planRouteForShipment(shipmentDTO);
        verifyNoInteractions(shipmentPricingService, shipmentPricingRepository, eventPublisher);
    }

    @Test
    void processingTheSameShipmentTwicePlansAndPricesOnlyOnce() {
        shipmentDTO.setMaxPrice(3000.0);
        when(shipmentPricingService.finalPrice(shipmentDTO, 2490.0)).thenReturn(new BigDecimal("2490.0"));
        when(shipmentRouteRepository.existsByShipmentId(7L)).thenReturn(false, true);

        shipmentService.processShipment(shipmentDTO);
        shipmentService.processShipment(shipmentDTO);

        verify(shipmentRoutePlanner, times(1)).planRouteForShipment(shipmentDTO);
        verify(shipmentPricingRepository, times(1)).save(any(ShipmentPricing.class));
        verify(eventPublisher, times(1)).publishEvent(any(ShipmentPricedEventDTO.class));
        verify(shipmentRepository, times(2)).save(shipment);
    }

    @Test
    void shipmentMessageKeepsTheFinalPriceBecauseItIsNeverTouched() {
        shipmentDTO.setDelivered(true);
        shipmentDTO.setPrice(null);
        shipmentDTO.setMaxPrice(3000.0);
        when(shipmentRouteRepository.existsByShipmentId(7L)).thenReturn(true);

        shipmentService.processShipment(shipmentDTO);

        verify(shipmentRepository).save(shipment);
        verify(shipmentRoutePlanner, never()).planRouteForShipment(any());
        verifyNoInteractions(shipmentPricingService, shipmentPricingRepository, eventPublisher);
    }

    @Test
    void unconfirmedShipmentIsOnlyUpsertedNotPlanned() {
        shipmentDTO.setConfirmed(false);

        shipmentService.processShipment(shipmentDTO);

        verify(shipmentRepository).save(shipment);
        verify(shipmentRoutePlanner, never()).planRouteForShipment(any());
        verifyNoInteractions(shipmentPricingService, shipmentPricingRepository, eventPublisher);
    }

    @Test
    void logisticsNeverSendsShipmentsToShipmentTopic() {
        assertThat(java.util.Arrays.stream(org.bme.micro_futar.logistics.kafka.KafkaProducerService.class.getMethods())
                .map(java.lang.reflect.Method::getName))
                .doesNotContain("sendShipment");
        assertThat(java.util.Arrays.stream(org.bme.micro_futar.logistics.kafka.KafkaBulkSenderService.class.getMethods())
                .map(java.lang.reflect.Method::getName))
                .doesNotContain("sendShipments");
    }

    private ShipmentRoute route(long id, ZonedDateTime fulfilledAt) {
        ShipmentRoute route = new ShipmentRoute();
        route.setId(id);
        route.setShipmentId(7L);
        route.setFulfillmentTime(fulfilledAt);
        return route;
    }

    private ShipmentRouteCourier assignment(long id, long routeId, boolean pickedUp) {
        ShipmentRouteCourier assignment = new ShipmentRouteCourier();
        assignment.setId(id);
        assignment.setShipmentRouteId(routeId);
        assignment.setPickedUpForDelivery(pickedUp);
        return assignment;
    }

    private void stubCanceledSaves() {
        lenient().when(shipmentRouteRepository.save(any(ShipmentRoute.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(shipmentRouteCourierRepository.save(any(ShipmentRouteCourier.class)))
                .thenAnswer(i -> i.getArgument(0));
        lenient().when(shipmentRouteMapper.toDTO(any(ShipmentRoute.class))).thenReturn(new ShipmentRouteDTO());
        lenient().when(shipmentRouteCourierMapper.toDTO(any(ShipmentRouteCourier.class)))
                .thenReturn(new ShipmentRouteCourierDTO());
    }

    @Test
    void canceledShipmentWithoutRoutesIsNotPlanned() {
        shipmentDTO.setCanceled(true);
        shipmentDTO.setConfirmed(false);
        when(shipmentRouteRepository.findByShipmentId(7L)).thenReturn(List.of());

        shipmentService.processShipment(shipmentDTO);

        verify(shipmentRepository).save(shipment);
        verify(shipmentRoutePlanner, never()).planRouteForShipment(any());
        verifyNoInteractions(shipmentPricingService, shipmentPricingRepository, eventPublisher);
    }

    @Test
    void canceledShipmentCancelsUnfulfilledRoutesAndAssignmentsAndPublishesThem() {
        shipmentDTO.setCanceled(true);
        ShipmentRoute open = route(1L, null);
        ShipmentRoute done = route(2L, ZonedDateTime.now());
        ShipmentRouteCourier openAssignment = assignment(10L, 1L, false);
        ShipmentRouteCourier doneAssignment = assignment(11L, 2L, false);
        when(shipmentRouteRepository.findByShipmentId(7L)).thenReturn(List.of(open, done));
        when(shipmentRouteCourierRepository.findByShipmentRouteIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(openAssignment, doneAssignment));
        stubCanceledSaves();

        shipmentService.processShipment(shipmentDTO);

        assertThat(open.isCanceled()).isTrue();
        assertThat(done.isCanceled()).isFalse();
        assertThat(openAssignment.isCanceled()).isTrue();
        assertThat(doneAssignment.isCanceled()).isFalse();
        verify(eventPublisher, times(1)).publishEvent(any(ShipmentRouteDTO.class));
        verify(eventPublisher, times(1)).publishEvent(any(ShipmentRouteCourierDTO.class));
        verify(shipmentRouteRepository, never()).delete(any());
        verify(shipmentRoutePlanner, never()).planRouteForShipment(any());
        verifyNoInteractions(shipmentManualReviewService);
    }

    @Test
    void canceledShipmentMessageRepeatedChangesAndPublishesNothingMore() {
        shipmentDTO.setCanceled(true);
        ShipmentRoute open = route(1L, null);
        ShipmentRouteCourier openAssignment = assignment(10L, 1L, false);
        when(shipmentRouteRepository.findByShipmentId(7L)).thenReturn(List.of(open));
        when(shipmentRouteCourierRepository.findByShipmentRouteIdIn(List.of(1L))).thenReturn(List.of(openAssignment));
        stubCanceledSaves();

        shipmentService.processShipment(shipmentDTO);
        shipmentService.processShipment(shipmentDTO);

        verify(eventPublisher, times(1)).publishEvent(any(ShipmentRouteDTO.class));
        verify(eventPublisher, times(1)).publishEvent(any(ShipmentRouteCourierDTO.class));
    }

    @Test
    void canceledShipmentAlreadyPickedUpIsFlaggedForManualReviewAndNotCanceled() {
        shipmentDTO.setCanceled(true);
        ShipmentRoute open = route(1L, null);
        ShipmentRouteCourier pickedUp = assignment(10L, 1L, true);
        when(shipmentRouteRepository.findByShipmentId(7L)).thenReturn(List.of(open));
        when(shipmentRouteCourierRepository.findByShipmentRouteIdIn(List.of(1L))).thenReturn(List.of(pickedUp));

        shipmentService.processShipment(shipmentDTO);

        assertThat(open.isCanceled()).isFalse();
        assertThat(pickedUp.isCanceled()).isFalse();
        verify(shipmentManualReviewService).flag(7L, "Shipment was canceled after a courier had picked it up");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void canceledDeliveredShipmentIsFlaggedForManualReview() {
        shipmentDTO.setCanceled(true);
        shipmentDTO.setDelivered(true);
        when(shipmentRouteRepository.findByShipmentId(7L)).thenReturn(List.of(route(1L, ZonedDateTime.now())));
        when(shipmentRouteCourierRepository.findByShipmentRouteIdIn(List.of(1L))).thenReturn(List.of());

        shipmentService.processShipment(shipmentDTO);

        verify(shipmentManualReviewService).flag(7L, "Shipment was canceled after it had been delivered");
        verifyNoInteractions(eventPublisher);
    }
}

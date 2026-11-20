package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.entities.Shipment;
import org.bme.micro_futar.logistics.mappers.ShipmentMapper;
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
import java.util.Optional;

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
    private ShipmentRouteRepository shipmentRouteRepository;

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
        shipmentDTO.setCurrencyCode("HUF");
        shipment = new Shipment();
        lenient().when(shipmentMapper.toEntity(shipmentDTO)).thenReturn(shipment);
        lenient().when(shipmentRoutePlanner.planRouteForShipment(shipmentDTO)).thenReturn(2490.0);
    }

    @Test
    void paidShipmentIsPricedAfterRoutePlanning() {
        shipmentDTO.setMaxPrice(3000.0);
        when(shipmentPricingService.finalPrice(shipmentDTO, 2490.0)).thenReturn(new BigDecimal("2490.0"));

        shipmentService.processShipment(shipmentDTO);

        assertThat(shipment.getPrice()).isEqualTo(2490.0);
        verify(shipmentRepository).save(shipment);
        verify(eventPublisher).publishEvent(argThat((ShipmentPricedEventDTO event) ->
                event.getShipmentId().equals(7L)
                        && event.getFinalAmount().compareTo(new BigDecimal("2490")) == 0
                        && event.getCurrencyCode().equals("HUF")));
    }

    @Test
    void shipmentWithoutPaymentIsNotPriced() {
        shipmentService.processShipment(shipmentDTO);

        verify(shipmentRepository).save(any(Shipment.class));
        verifyNoInteractions(shipmentPricingService, eventPublisher);
    }

    @Test
    void processingTheSameShipmentTwicePlansAndPricesOnlyOnce() {
        shipmentDTO.setMaxPrice(3000.0);
        when(shipmentPricingService.finalPrice(shipmentDTO, 2490.0)).thenReturn(new BigDecimal("2490.0"));
        when(shipmentRouteRepository.existsByShipmentId(7L)).thenReturn(false, true);
        when(shipmentRepository.findById(7L)).thenReturn(Optional.of(savedWithPrice(2490.0)));

        shipmentService.processShipment(shipmentDTO);
        shipmentService.processShipment(shipmentDTO);

        verify(shipmentRoutePlanner, times(1)).planRouteForShipment(shipmentDTO);
        verify(eventPublisher, times(1)).publishEvent(any(ShipmentPricedEventDTO.class));
        verify(shipmentRepository, times(2)).save(shipment);
    }

    @Test
    void deliveredMessageWithoutPriceKeepsExistingFinalPriceAndCreatesNoRoutes() {
        shipmentDTO.setDelivered(true);
        shipmentDTO.setPrice(null);
        shipmentDTO.setMaxPrice(3000.0);
        when(shipmentRouteRepository.existsByShipmentId(7L)).thenReturn(true);
        when(shipmentRepository.findById(7L)).thenReturn(Optional.of(savedWithPrice(2490.0)));

        shipmentService.processShipment(shipmentDTO);

        assertThat(shipment.getPrice()).isEqualTo(2490.0);
        verify(shipmentRepository).save(shipment);
        verify(shipmentRoutePlanner, never()).planRouteForShipment(any());
        verifyNoInteractions(shipmentPricingService, eventPublisher);
    }

    private Shipment savedWithPrice(double price) {
        Shipment saved = new Shipment();
        saved.setId(7L);
        saved.setPrice(price);
        return saved;
    }
}

package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.entities.Shipment;
import org.bme.micro_futar.logistics.mappers.ShipmentMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentRepository;
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
        when(shipmentMapper.toEntity(shipmentDTO)).thenReturn(shipment);
        when(shipmentRoutePlanner.planRouteForShipment(shipmentDTO)).thenReturn(2490.0);
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
}

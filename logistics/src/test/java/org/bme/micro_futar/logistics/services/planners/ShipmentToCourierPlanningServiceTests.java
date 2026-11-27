package org.bme.micro_futar.logistics.services.planners;

import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.services.*;
import org.bme.micro_futar.shared.dtos.*;
import org.bme.micro_futar.shared.enums.CourierType;
import org.bme.micro_futar.shared.enums.TransportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentToCourierPlanningServiceTests {

    private static final long DEPO = 1L;

    @Mock private CourierService courierService;
    @Mock private ShipmentRouteService shipmentRouteService;
    @Mock private ShipmentRouteCourierService shipmentRouteCourierService;
    @Mock private VehicleService vehicleService;
    @Mock private ShipmentService shipmentService;
    @Mock private PackageSizeService packageSizeService;

    private ShipmentToCourierPlanningService service;

    @BeforeEach
    void setUp() {
        service = new ShipmentToCourierPlanningService(courierService, shipmentRouteService, shipmentRouteCourierService,
                vehicleService, shipmentService, packageSizeService);
    }

    private CourierDTO courier(long id, TransportType type) {
        CourierDTO c = TestFixtures.courier(DEPO, id);
        c.setId(id);
        c.setQualifiedFor(type);
        return c;
    }

    private void stubCommon(List<CourierDTO> couriers, ShipmentRouteDTO route, CourierType courierType) {
        when(courierService.getCouriersByDepoIdAndType(DEPO, courierType)).thenReturn(couriers);
        VehicleDTO vehicle = TestFixtures.vehicle();
        vehicle.setMaximumPackableVolume(1000.0);
        when(vehicleService.getVehicleById(anyLong())).thenReturn(Optional.of(vehicle));
        ShipmentDTO shipment = TestFixtures.shipment(5L).build();
        when(shipmentService.getShipmentById(any())).thenReturn(shipment);
        PackageSizeDTO size = TestFixtures.smallPackage().id(TestFixtures.SMALL_PACKAGE_ID).maxLength(1.0).build();
        when(packageSizeService.getPackageSizeById(TestFixtures.SMALL_PACKAGE_ID)).thenReturn(Optional.of(size));
        when(shipmentRouteCourierService.countByCourierIdAndDate(anyLong(), any())).thenReturn(0L);
        when(shipmentRouteCourierService.findByCourierIdAndDateAssignedFor(anyLong(), any())).thenReturn(List.of());
    }

    private ShipmentRouteDTO crossDepoRoute(TransportType type) {
        return ShipmentRouteDTO.builder().id(10L).shipmentId(5L).originDepoId(DEPO).destinationDepoId(2L)
                .routePartNumber(1).transportType(type).build();
    }

    @Test
    void airLegIsAssignedOnlyToAnAirCourier() {
        ShipmentRouteDTO route = crossDepoRoute(TransportType.AIR);
        stubCommon(List.of(courier(1, TransportType.ROAD), courier(2, TransportType.AIR)), route, CourierType.CROSS_DEPO);
        when(shipmentRouteService.getCrossDepoRoutes(eq(DEPO), any())).thenReturn(List.of(route));

        service.planCrossDepoShipmentsForDepo(DEPO);

        ArgumentCaptor<ShipmentRouteCourierDTO> saved = ArgumentCaptor.forClass(ShipmentRouteCourierDTO.class);
        verify(shipmentRouteCourierService).save(saved.capture());
        assertThat(saved.getValue().getCourierId()).isEqualTo(2L);
    }

    @Test
    void airLegStaysUnassignedWithoutAnAirCourier() {
        ShipmentRouteDTO route = crossDepoRoute(TransportType.AIR);
        stubCommon(List.of(courier(1, TransportType.ROAD)), route, CourierType.CROSS_DEPO);
        when(shipmentRouteService.getCrossDepoRoutes(eq(DEPO), any())).thenReturn(List.of(route));

        service.planCrossDepoShipmentsForDepo(DEPO);

        verify(shipmentRouteCourierService, never()).save(any());
    }

    @Test
    void legWithoutTransportTypeIsTreatedAsRoad() {
        ShipmentRouteDTO route = crossDepoRoute(null);
        stubCommon(List.of(courier(1, TransportType.AIR), courier(2, TransportType.ROAD)), route, CourierType.CROSS_DEPO);
        when(shipmentRouteService.getCrossDepoRoutes(eq(DEPO), any())).thenReturn(List.of(route));

        service.planCrossDepoShipmentsForDepo(DEPO);

        ArgumentCaptor<ShipmentRouteCourierDTO> saved = ArgumentCaptor.forClass(ShipmentRouteCourierDTO.class);
        verify(shipmentRouteCourierService).save(saved.capture());
        assertThat(saved.getValue().getCourierId()).isEqualTo(2L);
    }

    @Test
    void courierWithoutQualificationIsTreatedAsRoadCourier() {
        ShipmentRouteDTO route = crossDepoRoute(TransportType.ROAD);
        stubCommon(List.of(courier(1, null)), route, CourierType.CROSS_DEPO);
        when(shipmentRouteService.getCrossDepoRoutes(eq(DEPO), any())).thenReturn(List.of(route));

        service.planCrossDepoShipmentsForDepo(DEPO);

        verify(shipmentRouteCourierService).save(any());
    }

    @Test
    void planningClosesStaleAssignmentsBeforeLookingForLegs() {
        when(courierService.getCouriersByDepoIdAndType(DEPO, CourierType.DELIVERY)).thenReturn(List.of(courier(1, TransportType.ROAD)));
        when(shipmentRouteService.getDeliveryRoutes(eq(DEPO), any())).thenReturn(List.of());
        when(shipmentRouteService.getPickupRoutes(eq(DEPO), any())).thenReturn(List.of());

        service.planShipmentsForDepo(DEPO);

        var order = inOrder(shipmentRouteCourierService, shipmentRouteService);
        order.verify(shipmentRouteCourierService).closeStaleAssignments(LocalDate.now());
        order.verify(shipmentRouteService).getDeliveryRoutes(DEPO, LocalDate.now());
    }
}

package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.EnableTestContainers;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Which legs are offered to the courier planning; needs Docker. */
@EnableTestContainers
@SpringBootTest
@Transactional
class ShipmentRouteRepositoryTests {

    private static final long DEPO = 7L;
    private static final long OTHER_DEPO = 8L;
    private final LocalDate today = LocalDate.now();

    @Autowired private ShipmentRouteRepository routes;
    @Autowired private ShipmentRouteCourierRepository assignments;

    private ShipmentRoute route(long shipmentId, int part, Long origin, Long destination, String originAddr, String destAddr, boolean fulfilled) {
        ShipmentRoute r = new ShipmentRoute();
        r.setShipmentId(shipmentId);
        r.setRoutePartNumber(part);
        r.setOriginDepoId(origin);
        r.setDestinationDepoId(destination);
        r.setOriginAddress(originAddr);
        r.setDestinationAddress(destAddr);
        if (fulfilled) {
            r.setFulfillmentTime(ZonedDateTime.now());
        }
        return routes.saveAndFlush(r);
    }

    private ShipmentRouteCourier assign(ShipmentRoute route, LocalDate date, boolean failed, boolean pickedUp, boolean canceled) {
        ShipmentRouteCourier a = new ShipmentRouteCourier();
        a.setCourierId(1L);
        a.setShipmentRouteId(route.getId());
        a.setDateAssignedFor(date);
        a.setFailed(failed);
        a.setPickedUpForDelivery(pickedUp);
        a.setCanceled(canceled);
        return assignments.saveAndFlush(a);
    }

    private ShipmentRoute pickup(long shipmentId) {
        return route(shipmentId, 0, null, DEPO, "A", null, false);
    }

    private ShipmentRoute delivery(long shipmentId, int part, boolean previousFulfilled) {
        route(shipmentId, part - 1, OTHER_DEPO, DEPO, null, null, previousFulfilled);
        return route(shipmentId, part, DEPO, null, null, "B", false);
    }

    @Test
    void secondPlanningRunTheSameDayDoesNotOfferAnAlreadyAssignedLeg() {
        ShipmentRoute pickup = pickup(1);
        assertThat(routes.findPickupRoutes(DEPO, today)).extracting(ShipmentRoute::getId).containsExactly(pickup.getId());

        assign(pickup, today, false, false, false);

        assertThat(routes.findPickupRoutes(DEPO, today)).isEmpty();
    }

    @Test
    void failedAssignmentLeavesTheLegAssignableAgain() {
        ShipmentRoute delivery = delivery(2, 1, true);
        assign(delivery, today.minusDays(1), true, true, false);

        assertThat(routes.findDeliveryRoutes(DEPO, today)).extracting(ShipmentRoute::getId).containsExactly(delivery.getId());
    }

    @Test
    void canceledAssignmentDoesNotBlockButLiveOneDoes() {
        ShipmentRoute delivery = delivery(3, 1, true);
        assign(delivery, today, false, false, true);
        assertThat(routes.findDeliveryRoutes(DEPO, today)).hasSize(1);

        assign(delivery, today, false, false, false);
        assertThat(routes.findDeliveryRoutes(DEPO, today)).isEmpty();
    }

    @Test
    void deliveryIsNotOfferedUntilThePreviousPartIsFulfilled() {
        ShipmentRoute notReady = delivery(4, 1, false);
        ShipmentRoute ready = delivery(5, 1, true);

        assertThat(routes.findDeliveryRoutes(DEPO, today)).extracting(ShipmentRoute::getId)
                .contains(ready.getId()).doesNotContain(notReady.getId());
    }

    @Test
    void stalePastAssignmentNeverPickedUpIsFoundForClosingAndNoLongerBlocks() {
        ShipmentRoute pickup = pickup(6);
        ShipmentRouteCourier stale = assign(pickup, today.minusDays(1), false, false, false);

        assertThat(routes.findPickupRoutes(DEPO, today)).hasSize(1);
        assertThat(assignments.findStaleUnfinished(today)).extracting(ShipmentRouteCourier::getId).contains(stale.getId());
    }

    @Test
    void pastAssignmentAlreadyPickedUpStillBlocksAndIsNotClosed() {
        ShipmentRoute delivery = delivery(7, 1, true);
        ShipmentRouteCourier held = assign(delivery, today.minusDays(1), false, true, false);

        assertThat(routes.findDeliveryRoutes(DEPO, today)).isEmpty();
        assertThat(assignments.findStaleUnfinished(today)).extracting(ShipmentRouteCourier::getId).doesNotContain(held.getId());
    }
}

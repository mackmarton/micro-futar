package org.bme.micro_futar.logistics.kafka;

import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ShipmentRouteFulfilledReplicationTests {

    private static final long SHIPMENT_ID = 44L;
    private static final ZonedDateTime FULFILLED_AT = ZonedDateTime.parse("2026-11-26T14:35:00+01:00");

    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRouteRepository routes;
    @Autowired private ShipmentManualReviewRepository reviews;

    @Test
    void fulfilmentTimeIsStoredAndTheRouteAnnounced() {
        Long id = givenLeg(false);

        report(id, FULFILLED_AT);

        assertThat(stored(id).getFulfillmentTime().toInstant()).isEqualTo(FULFILLED_AT.toInstant());
        assertThat(kafka.received("shipment-route-topic", ShipmentRouteDTO.class)).singleElement()
                .satisfies(route -> assertThat(route.getFulfillmentTime()).isNotNull());
        assertThat(reviews.findAll()).isEmpty();
    }

    @Test
    void redeliveredEventKeepsTheFirstTime() {
        Long id = givenLeg(false);
        report(id, FULFILLED_AT);

        report(id, FULFILLED_AT.plusHours(1));

        assertThat(stored(id).getFulfillmentTime().toInstant()).isEqualTo(FULFILLED_AT.toInstant());
        assertThat(kafka.received("shipment-route-topic")).hasSize(1);
    }

    @Test
    void unknownRouteIsIgnored() {
        report(404_404L, FULFILLED_AT);

        assertThat(routes.findAll()).isEmpty();
        assertThat(kafka.received("shipment-route-topic")).isEmpty();
    }

    /** The courier's report is a fact and is kept, but the canceled shipment physically moved on. */
    @Test
    void fulfilmentAfterCancellationGoesToManualReview() {
        Long id = givenLeg(true);

        report(id, FULFILLED_AT);
        report(id, FULFILLED_AT);

        assertThat(stored(id).getFulfillmentTime()).isNotNull();
        assertThat(kafka.received("shipment-route-topic")).hasSize(1);
        assertThat(reviews.findAll()).singleElement()
                .extracting(ShipmentManualReview::getShipmentId, ShipmentManualReview::getReason)
                .containsExactly(SHIPMENT_ID, "Leg fulfilled after cancellation");
    }

    private Long givenLeg(boolean canceled) {
        ShipmentRoute leg = TestFixtures.route(SHIPMENT_ID, 0);
        leg.setCanceled(canceled);
        return routes.save(leg).getId();
    }

    private ShipmentRoute stored(Long id) {
        return routes.findById(id).orElseThrow();
    }

    private void report(Long routeId, ZonedDateTime fulfilledAt) {
        var event = ShipmentRouteFulfilledEventDTO.builder()
                .shipmentRouteId(routeId)
                .shipmentId(SHIPMENT_ID)
                .fulfilledAt(fulfilledAt)
                .build();
        kafka.sendAndAwaitConsumption("shipment-route-fulfilled-topic", routeId.toString(), event);
    }
}

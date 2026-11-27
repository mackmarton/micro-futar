package org.bme.micro_futar.logistics.kafka;

import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.services.ShipmentManualReviewService;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** A canceled shipment: what logistics planned is canceled, unless the parcel is already out of its hands. */
@IntegrationTest
class ShipmentCancellationReplicationTests {

    private static final long SHIPMENT_ID = 42L;
    private static final String PICKED_UP = "Shipment was canceled after a courier had picked it up";

    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRepository shipments;
    @Autowired private ShipmentRouteRepository routes;
    @Autowired private ShipmentRouteCourierRepository assignments;
    @Autowired private ShipmentManualReviewRepository reviews;
    @Autowired private ShipmentManualReviewService reviewService;

    @Test
    void canceledShipmentWithoutRoutesIsNotPlanned() {
        publish(canceled().confirmed(true).build());

        assertThat(shipments.findById(SHIPMENT_ID)).get().extracting("canceled").isEqualTo(true);
        assertThat(routes.findAll()).isEmpty();
        assertThat(reviews.findAll()).isEmpty();
        assertThat(kafka.received("shipment-route-topic")).isEmpty();
    }

    @Test
    void cancellationCancelsOpenLegsAndAssignmentsAndAnnouncesThem() {
        ShipmentRoute leg = givenLeg(0, false);
        ShipmentRouteCourier assignment = givenAssignment(leg, false);

        publish(canceled().build());

        assertThat(stored(leg).isCanceled()).isTrue();
        assertThat(stored(assignment).isCanceled()).isTrue();
        assertThat(kafka.received("shipment-route-topic", ShipmentRouteDTO.class)).singleElement()
                .satisfies(route -> assertThat(route.isCanceled()).isTrue());
        assertThat(kafka.received("shipment-route-courier-topic", ShipmentRouteCourierDTO.class)).singleElement()
                .satisfies(announced -> assertThat(announced.isCanceled()).isTrue());
        assertThat(reviews.findAll()).isEmpty();
    }

    @Test
    void fulfilledLegsAndTheirAssignmentsAreLeftAlone() {
        ShipmentRoute done = givenLeg(0, true);
        ShipmentRouteCourier doneAssignment = givenAssignment(done, false);
        ShipmentRoute open = givenLeg(1, false);

        publish(canceled().build());

        assertThat(stored(done).isCanceled()).isFalse();
        assertThat(stored(doneAssignment).isCanceled()).isFalse();
        assertThat(stored(open).isCanceled()).isTrue();
        assertThat(kafka.received("shipment-route-topic")).hasSize(1);
        assertThat(kafka.received("shipment-route-courier-topic")).isEmpty();
    }

    @Test
    void redeliveredCancellationAnnouncesNothingMore() {
        givenAssignment(givenLeg(0, false), false);
        publish(canceled().build());

        publish(canceled().build());

        assertThat(kafka.received("shipment-route-topic")).hasSize(1);
        assertThat(kafka.received("shipment-route-courier-topic")).hasSize(1);
    }

    @Test
    void pickedUpShipmentGoesToManualReviewInsteadOfBeingCanceled() {
        ShipmentRoute leg = givenLeg(0, false);
        ShipmentRouteCourier assignment = givenAssignment(leg, true);

        publish(canceled().build());

        assertThat(stored(leg).isCanceled()).isFalse();
        assertThat(stored(assignment).isCanceled()).isFalse();
        assertThat(reviews.findAll()).singleElement().satisfies(review -> {
            assertThat(review.getShipmentId()).isEqualTo(SHIPMENT_ID);
            assertThat(review.getReason()).isEqualTo(PICKED_UP);
            assertThat(review.getFlaggedAt()).isNotNull();
        });
        assertThat(kafka.received("shipment-route-topic")).isEmpty();
        assertThat(kafka.received("shipment-route-courier-topic")).isEmpty();
    }

    @Test
    void deliveredShipmentGoesToManualReview() {
        ShipmentRoute leg = givenLeg(0, true);

        publish(canceled().delivered(true).build());

        assertThat(reviews.findAll()).singleElement()
                .extracting(ShipmentManualReview::getReason)
                .isEqualTo("Shipment was canceled after it had been delivered");
        assertThat(stored(leg).isCanceled()).isFalse();
        assertThat(kafka.received("shipment-route-topic")).isEmpty();
    }

    @Test
    void shipmentStaysOnTheListOnceWithItsFirstReason() {
        givenAssignment(givenLeg(0, false), true);
        publish(canceled().build());
        ShipmentManualReview first = reviews.findById(SHIPMENT_ID).orElseThrow();

        publish(canceled().delivered(true).build());

        assertThat(reviews.findAll()).singleElement().satisfies(review -> {
            assertThat(review.getReason()).isEqualTo(PICKED_UP);
            assertThat(review.getFlaggedAt()).isEqualTo(first.getFlaggedAt());
        });
    }

    @Test
    void redeliveredCancellationDoesNotReopenAResolvedItem() {
        givenAssignment(givenLeg(0, false), true);
        publish(canceled().build());
        reviewService.resolve(SHIPMENT_ID, TestFixtures.ADMIN_EMAIL);

        publish(canceled().build());

        assertThat(reviews.findAll()).singleElement()
                .extracting(ShipmentManualReview::getResolvedBy).isEqualTo(TestFixtures.ADMIN_EMAIL);
    }

    private ShipmentDTO.ShipmentDTOBuilder canceled() {
        return TestFixtures.shipment(SHIPMENT_ID).confirmed(false).canceled(true);
    }

    private ShipmentRoute givenLeg(int partNumber, boolean fulfilled) {
        ShipmentRoute leg = TestFixtures.route(SHIPMENT_ID, partNumber);
        if (fulfilled) {
            leg.setFulfillmentTime(ZonedDateTime.now());
        }
        return routes.save(leg);
    }

    private ShipmentRouteCourier givenAssignment(ShipmentRoute leg, boolean pickedUp) {
        ShipmentRouteCourier assignment = TestFixtures.assignment(leg.getId(), LocalDate.now());
        assignment.setPickedUpForDelivery(pickedUp);
        return assignments.save(assignment);
    }

    private ShipmentRoute stored(ShipmentRoute leg) {
        return routes.findById(leg.getId()).orElseThrow();
    }

    private ShipmentRouteCourier stored(ShipmentRouteCourier assignment) {
        return assignments.findById(assignment.getId()).orElseThrow();
    }

    private void publish(ShipmentDTO shipment) {
        kafka.sendAndAwaitConsumption("shipment-topic", shipment.getId().toString(), shipment);
    }
}

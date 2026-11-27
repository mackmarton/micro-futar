package org.bme.micro_futar.logistics.kafka;

import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.enums.AssignmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Courier reports what happened to an assignment; logistics owns it, applies the fact and announces it. */
@IntegrationTest
class AssignmentStatusReplicationTests {

    private static final long SHIPMENT_ID = 43L;

    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRouteRepository routes;
    @Autowired private ShipmentRouteCourierRepository assignments;
    @Autowired private ShipmentManualReviewRepository reviews;

    @Test
    void pickUpIsRecordedAndAnnouncedOnce() {
        Long id = givenAssignment(false);

        report(id, AssignmentStatus.PICKED_UP);
        report(id, AssignmentStatus.PICKED_UP);

        assertThat(stored(id).getPickedUpForDelivery()).isTrue();
        assertThat(kafka.received("shipment-route-courier-topic", ShipmentRouteCourierDTO.class)).singleElement()
                .extracting(ShipmentRouteCourierDTO::getPickedUpForDelivery).isEqualTo(true);
        assertThat(reviews.findAll()).isEmpty();
    }

    @Test
    void failureIsRecordedAndAnnouncedOnce() {
        Long id = givenAssignment(false);
        report(id, AssignmentStatus.PICKED_UP);

        report(id, AssignmentStatus.FAILED);
        report(id, AssignmentStatus.FAILED);

        assertThat(stored(id).getFailed()).isTrue();
        assertThat(stored(id).getPickedUpForDelivery()).isTrue();
        // one for the pick-up, one for the failure
        assertThat(kafka.received("shipment-route-courier-topic")).hasSize(2);
    }

    @Test
    void unknownAssignmentIsIgnored() {
        report(404_404L, AssignmentStatus.PICKED_UP);

        assertThat(assignments.findAll()).isEmpty();
        assertThat(kafka.received("shipment-route-courier-topic")).isEmpty();
    }

    @Test
    void pickUpAfterCancellationGoesToManualReview() {
        Long id = givenAssignment(true);

        report(id, AssignmentStatus.PICKED_UP);
        report(id, AssignmentStatus.PICKED_UP);

        assertThat(stored(id).getPickedUpForDelivery()).isTrue();
        assertThat(stored(id).isCanceled()).isTrue();
        assertThat(kafka.received("shipment-route-courier-topic")).hasSize(1);
        assertThat(reviews.findAll()).singleElement()
                .extracting(ShipmentManualReview::getShipmentId, ShipmentManualReview::getReason)
                .containsExactly(SHIPMENT_ID, "Picked up after cancellation");
    }

    @Test
    void failureAfterCancellationNeedsNoReview() {
        Long id = givenAssignment(true);

        report(id, AssignmentStatus.FAILED);

        assertThat(stored(id).getFailed()).isTrue();
        assertThat(reviews.findAll()).isEmpty();
    }

    private Long givenAssignment(boolean canceled) {
        ShipmentRoute leg = routes.save(TestFixtures.route(SHIPMENT_ID, 0));
        ShipmentRouteCourier assignment = TestFixtures.assignment(leg.getId(), LocalDate.now());
        assignment.setCanceled(canceled);
        return assignments.save(assignment).getId();
    }

    private ShipmentRouteCourier stored(Long id) {
        return assignments.findById(id).orElseThrow();
    }

    private void report(Long assignmentId, AssignmentStatus status) {
        var event = AssignmentStatusEventDTO.builder()
                .assignmentId(assignmentId)
                .status(status)
                .occurredAt(ZonedDateTime.now())
                .build();
        kafka.sendAndAwaitConsumption("assignment-status-topic", assignmentId.toString(), event);
    }
}

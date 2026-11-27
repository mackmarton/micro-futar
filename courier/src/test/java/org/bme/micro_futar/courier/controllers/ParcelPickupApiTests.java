package org.bme.micro_futar.courier.controllers;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.entities.AssignmentProgress;
import org.bme.micro_futar.courier.repositories.AssignmentProgressRepository;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.KafkaTestClient;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.courier.support.TestJwts;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.enums.AssignmentStatus;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.bme.micro_futar.courier.support.CourierScenario.DELIVERY;
import static org.bme.micro_futar.courier.support.CourierScenario.PICKUP;
import static org.bme.micro_futar.courier.support.CourierScenario.delivery;
import static org.bme.micro_futar.courier.support.CourierScenario.pickup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ParcelPickupApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private CourierScenario scenario;
    @Autowired private AssignmentProgressRepository progress;

    @BeforeEach
    void givenAPickupAndADeliveryForToday() {
        scenario.publish(TestFixtures.courier());
        scenario.publish(TestFixtures.shipment().build());
        scenario.publish(TestFixtures.pickupLeg().build());
        scenario.publish(TestFixtures.deliveryLeg().build());
        scenario.publish(pickup().build());
        scenario.publish(delivery().build());
    }

    @Test
    void pickingUpAPickupLegIsStoredAndReported() throws Exception {
        act("/pickup/" + PICKUP);

        assertThat(progress.findById(PICKUP)).get()
                .extracting(AssignmentProgress::getPickedUpAt).isNotNull();
        assertThat(kafka.received("assignment-status-topic")).singleElement()
                .satisfies(record -> assertThat(record.key()).isEqualTo("1"));
        assertThat(kafka.received("assignment-status-topic", AssignmentStatusEventDTO.class)).singleElement()
                .satisfies(event -> {
                    assertThat(event.getAssignmentId()).isEqualTo(PICKUP);
                    assertThat(event.getShipmentRouteId()).isEqualTo(TestFixtures.PICKUP_ROUTE_ID);
                    assertThat(event.getStatus()).isEqualTo(AssignmentStatus.PICKED_UP);
                    assertThat(event.getOccurredAt()).isNotNull();
                });
        assertThat(kafka.received("shipment-notification-topic")).isEmpty();
        mockMvc.perform(get("/api/me/assignments/" + PICKUP)
                        .with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(jsonPath("$.pickedUpForDelivery").value(true));
    }

    @Test
    void pickingUpADeliveryLegNotifiesThatTheParcelIsOutForDelivery() throws Exception {
        act("/pickup/" + DELIVERY);

        await().untilAsserted(() -> assertThat(
                kafka.received("shipment-notification-topic", ShipmentNotificationEventDTO.class))
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.getType()).isEqualTo(NotificationType.OUT_FOR_DELIVERY);
                    assertThat(notification.getDedupKey()).isEqualTo("OUT_FOR_DELIVERY:assignment:2");
                    assertThat(notification.getRecipientEmail()).isEqualTo(TestFixtures.RECIPIENT_EMAIL);
                }));
    }

    @Test
    void repeatedPickupIsReportedOnlyOnce() throws Exception {
        act("/pickup/" + DELIVERY);
        var firstPickup = progress.findById(DELIVERY).orElseThrow().getPickedUpAt();

        act("/pickup/" + DELIVERY);

        assertThat(progress.findById(DELIVERY).orElseThrow().getPickedUpAt()).isEqualTo(firstPickup);
        assertThat(kafka.received("assignment-status-topic")).hasSize(1);
        await().untilAsserted(() -> assertThat(kafka.received("shipment-notification-topic")).hasSize(1));
    }

    @Test
    void pickingUpAllDeliveriesTakesOnlyOwnActiveDeliveries() throws Exception {
        scenario.publish(TestFixtures.otherCourier());
        scenario.publish(delivery().id(3L).courierId(TestFixtures.OTHER_COURIER_ID).build());
        scenario.publish(delivery().id(4L).dateAssignedFor(LocalDate.now().minusDays(1)).build());
        scenario.publish(delivery().id(5L).canceled(true).build());

        act("/pickup-all-deliveries-for-today");

        assertThat(progress.findAll()).singleElement()
                .extracting(AssignmentProgress::getAssignmentId).isEqualTo(DELIVERY);
        assertThat(kafka.received("assignment-status-topic", AssignmentStatusEventDTO.class)).singleElement()
                .extracting(AssignmentStatusEventDTO::getAssignmentId).isEqualTo(DELIVERY);
    }

    @Test
    void canceledAssignmentCannotBePickedUp() throws Exception {
        scenario.publish(TestFixtures.shipment().confirmed(false).canceled(true).build());
        scenario.publish(delivery().canceled(true).build());

        act("/pickup/" + DELIVERY);

        assertThat(progress.findAll()).isEmpty();
        assertThat(kafka.received("assignment-status-topic")).isEmpty();
        assertThat(kafka.received("shipment-notification-topic")).isEmpty();
    }

    private ResultActions act(String url) throws Exception {
        return mockMvc.perform(post("/api/shipment-route-couriers" + url)
                        .with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isOk());
    }
}

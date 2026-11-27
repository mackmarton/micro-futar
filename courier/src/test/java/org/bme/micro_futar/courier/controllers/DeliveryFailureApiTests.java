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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.bme.micro_futar.courier.support.CourierScenario.DELIVERY;
import static org.bme.micro_futar.courier.support.CourierScenario.PICKUP;
import static org.bme.micro_futar.courier.support.CourierScenario.delivery;
import static org.bme.micro_futar.courier.support.CourierScenario.pickup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class DeliveryFailureApiTests {

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
    void failedDeliveryIsStoredReportedAndNotified() throws Exception {
        act("/" + DELIVERY + "/fail");

        assertThat(progress.findById(DELIVERY)).get()
                .extracting(AssignmentProgress::getFailedAt).isNotNull();
        assertThat(kafka.received("assignment-status-topic", AssignmentStatusEventDTO.class)).singleElement()
                .satisfies(event -> {
                    assertThat(event.getAssignmentId()).isEqualTo(DELIVERY);
                    assertThat(event.getShipmentRouteId()).isEqualTo(TestFixtures.DELIVERY_ROUTE_ID);
                    assertThat(event.getStatus()).isEqualTo(AssignmentStatus.FAILED);
                });
        await().untilAsserted(() -> assertThat(
                kafka.received("shipment-notification-topic", ShipmentNotificationEventDTO.class))
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.getType()).isEqualTo(NotificationType.DELIVERY_FAILED);
                    assertThat(notification.getDedupKey()).isEqualTo("DELIVERY_FAILED:assignment:2");
                }));
    }

    @Test
    void failedPickupAttemptIsReportedWithoutNotification() throws Exception {
        act("/" + PICKUP + "/fail");

        assertThat(kafka.received("assignment-status-topic", AssignmentStatusEventDTO.class)).singleElement()
                .extracting(AssignmentStatusEventDTO::getStatus).isEqualTo(AssignmentStatus.FAILED);
        assertThat(kafka.received("shipment-notification-topic")).isEmpty();
    }

    @Test
    void repeatedFailureIsReportedOnlyOnce() throws Exception {
        act("/" + DELIVERY + "/fail");

        act("/" + DELIVERY + "/fail");

        assertThat(kafka.received("assignment-status-topic")).hasSize(1);
        await().untilAsserted(() -> assertThat(kafka.received("shipment-notification-topic")).hasSize(1));
    }

    @Test
    void canceledAssignmentNeverPickedUpCannotBeFailed() throws Exception {
        scenario.publish(TestFixtures.shipment().confirmed(false).canceled(true).build());
        scenario.publish(delivery().canceled(true).build());

        act("/" + DELIVERY + "/fail");

        assertThat(progress.findAll()).isEmpty();
        assertThat(kafka.received("assignment-status-topic")).isEmpty();
        assertThat(kafka.received("shipment-notification-topic")).isEmpty();
    }

    @Test
    void canceledAssignmentAlreadyPickedUpCanStillBeFailed() throws Exception {
        act("/pickup/" + DELIVERY);
        scenario.publish(TestFixtures.shipment().confirmed(false).canceled(true).build());
        scenario.publish(delivery().canceled(true).build());

        act("/" + DELIVERY + "/fail");
        act("/" + DELIVERY + "/fail");

        assertThat(progress.findById(DELIVERY)).get()
                .extracting(AssignmentProgress::getFailedAt).isNotNull();
        assertThat(kafka.received("assignment-status-topic", AssignmentStatusEventDTO.class))
                .extracting(AssignmentStatusEventDTO::getStatus)
                .containsExactly(AssignmentStatus.PICKED_UP, AssignmentStatus.FAILED);
    }

    private ResultActions act(String url) throws Exception {
        return mockMvc.perform(post("/api/shipment-route-couriers" + url)
                        .with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isOk());
    }
}

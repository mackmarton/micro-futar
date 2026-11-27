package org.bme.micro_futar.courier.controllers;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.entities.AssignmentProgress;
import org.bme.micro_futar.courier.repositories.AssignmentProgressRepository;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.KafkaTestClient;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.courier.support.TestJwts;
import org.bme.micro_futar.shared.dtos.ShipmentDeliveredEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ParcelDeliveryApiTests {

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
    void deliveringReportsTheLegAndTheShipmentAndNotifies() throws Exception {
        act("/" + DELIVERY + "/fulfill");

        assertThat(progress.findById(DELIVERY)).get()
                .extracting(AssignmentProgress::getFulfilledAt).isNotNull();
        var fulfilled = kafka.received("shipment-route-fulfilled-topic", ShipmentRouteFulfilledEventDTO.class);
        var delivered = kafka.received("shipment-delivered-topic", ShipmentDeliveredEventDTO.class);
        assertThat(fulfilled).singleElement().satisfies(event -> {
            assertThat(event.getShipmentRouteId()).isEqualTo(TestFixtures.DELIVERY_ROUTE_ID);
            assertThat(event.getShipmentId()).isEqualTo(TestFixtures.SHIPMENT_ID);
            assertThat(event.getFulfilledAt()).isNotNull();
        });
        assertThat(delivered).singleElement().satisfies(event -> {
            assertThat(event.getShipmentId()).isEqualTo(TestFixtures.SHIPMENT_ID);
            assertThat(event.getDeliveredAt()).isEqualTo(fulfilled.getFirst().getFulfilledAt());
        });
        await().untilAsserted(() -> assertThat(
                kafka.received("shipment-notification-topic", ShipmentNotificationEventDTO.class))
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.getType()).isEqualTo(NotificationType.DELIVERED);
                    assertThat(notification.getDedupKey()).isEqualTo("DELIVERED:shipment:" + TestFixtures.SHIPMENT_ID);
                }));
    }

    @Test
    void fulfillingAPickupLegReportsOnlyTheLeg() throws Exception {
        act("/" + PICKUP + "/fulfill");

        assertThat(kafka.received("shipment-route-fulfilled-topic", ShipmentRouteFulfilledEventDTO.class))
                .singleElement()
                .extracting(ShipmentRouteFulfilledEventDTO::getShipmentRouteId)
                .isEqualTo(TestFixtures.PICKUP_ROUTE_ID);
        assertThat(kafka.received("shipment-delivered-topic")).isEmpty();
        assertThat(kafka.received("shipment-notification-topic")).isEmpty();
    }

    @Test
    void repeatedFulfilmentIsReportedOnlyOnce() throws Exception {
        act("/" + DELIVERY + "/fulfill");

        act("/" + DELIVERY + "/fulfill");

        assertThat(kafka.received("shipment-route-fulfilled-topic")).hasSize(1);
        assertThat(kafka.received("shipment-delivered-topic")).hasSize(1);
        await().untilAsserted(() -> assertThat(kafka.received("shipment-notification-topic")).hasSize(1));
    }

    @Test
    void fulfillingAllPickupsReportsOnlyPickedUpPickups() throws Exception {
        scenario.publish(TestFixtures.pickupLeg().id(3L).build());
        scenario.publish(pickup().id(3L).shipmentRouteId(3L).build());
        act("/pickup/" + PICKUP);
        act("/pickup/" + DELIVERY);

        act("/fulfill-all-pickups-for-tocay");

        assertThat(kafka.received("shipment-route-fulfilled-topic", ShipmentRouteFulfilledEventDTO.class))
                .singleElement()
                .extracting(ShipmentRouteFulfilledEventDTO::getShipmentRouteId)
                .isEqualTo(TestFixtures.PICKUP_ROUTE_ID);
        mockMvc.perform(get("/api/shipment-route-couriers/picked-up-assignments")
                        .with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(jsonPath("$[*].id").value((int) PICKUP));
    }

    @Test
    void canceledAssignmentNeverPickedUpCannotBeFulfilled() throws Exception {
        scenario.publish(TestFixtures.shipment().confirmed(false).canceled(true).build());
        scenario.publish(delivery().canceled(true).build());

        act("/" + DELIVERY + "/fulfill");

        assertThat(progress.findAll()).isEmpty();
        assertThat(kafka.received("shipment-route-fulfilled-topic")).isEmpty();
        assertThat(kafka.received("shipment-delivered-topic")).isEmpty();
    }

    @Test
    void canceledAssignmentAlreadyPickedUpCanStillBeDelivered() throws Exception {
        act("/pickup/" + DELIVERY);
        scenario.publish(TestFixtures.shipment().confirmed(false).canceled(true).build());
        scenario.publish(delivery().canceled(true).build());

        act("/" + DELIVERY + "/fulfill");
        act("/" + DELIVERY + "/fulfill");

        assertThat(progress.findById(DELIVERY)).get()
                .extracting(AssignmentProgress::getFulfilledAt).isNotNull();
        assertThat(kafka.received("shipment-route-fulfilled-topic")).hasSize(1);
        assertThat(kafka.received("shipment-delivered-topic")).hasSize(1);
    }

    @Test
    void deliveryOfAnUnknownShipmentSucceedsWithoutNotification() throws Exception {
        scenario.publish(TestFixtures.deliveryLeg().shipmentId(999L).build());

        act("/" + DELIVERY + "/fulfill");

        assertThat(kafka.received("shipment-delivered-topic")).hasSize(1);
        assertThat(kafka.received("shipment-notification-topic")).isEmpty();
    }

    private ResultActions act(String url) throws Exception {
        return mockMvc.perform(post("/api/shipment-route-couriers" + url)
                        .with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isOk());
    }
}

package org.bme.micro_futar.courier.kafka;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.bme.micro_futar.courier.repositories.AssignmentProgressRepository;
import org.bme.micro_futar.courier.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.KafkaTestClient;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.courier.support.TestJwts;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ShipmentRouteCourierReplicationTests {

    private static final long ASSIGNMENT = 1L;

    @Autowired private KafkaTestClient kafka;
    @Autowired private CourierScenario scenario;
    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private ShipmentRouteCourierRepository assignments;
    @Autowired private AssignmentProgressRepository progress;

    @Test
    void newAssignmentIsStored() {
        scenario.publish(assignment().build());

        assertThat(assignments.findById(ASSIGNMENT)).get().satisfies(stored -> {
            assertThat(stored.getCourierId()).isEqualTo(TestFixtures.COURIER_ID);
            assertThat(stored.getShipmentRouteId()).isEqualTo(TestFixtures.DELIVERY_ROUTE_ID);
            assertThat(stored.isCanceled()).isFalse();
        });
    }

    @Test
    void reassignedAssignmentOverwritesTheLocalCopy() {
        scenario.publish(assignment().build());

        scenario.publish(assignment().courierId(TestFixtures.OTHER_COURIER_ID).build());

        assertThat(assignments.findAll()).singleElement()
                .extracting(ShipmentRouteCourier::getCourierId).isEqualTo(TestFixtures.OTHER_COURIER_ID);
    }

    @Test
    void canceledAssignmentLeavesTheCouriersList() throws Exception {
        CourierDTO courier = TestFixtures.courier();
        kafka.sendAndAwaitConsumption("courier-topic", courier.getId().toString(), courier);
        scenario.publish(assignment().build());

        scenario.publish(assignment().canceled(true).build());

        mockMvc.perform(get("/api/shipment-route-couriers")
                        .with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void redeliveredAssignmentKeepsASingleCopy() {
        scenario.publish(assignment().build());

        scenario.publish(assignment().build());

        assertThat(assignments.findAll()).singleElement()
                .extracting(ShipmentRouteCourier::getCourierId).isEqualTo(TestFixtures.COURIER_ID);
    }

    @Test
    void laterMessageFromLogisticsKeepsTheCouriersProgress() throws Exception {
        CourierDTO courier = TestFixtures.courier();
        kafka.sendAndAwaitConsumption("courier-topic", courier.getId().toString(), courier);
        scenario.publish(assignment().build());
        assertThat(progress.findAll()).isEmpty();
        mockMvc.perform(post("/api/shipment-route-couriers/pickup/" + ASSIGNMENT)
                        .with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isOk());

        // logistics has not processed the pickup yet, so its copy still says nothing was picked up
        scenario.publish(assignment().pickedUpForDelivery(false).build());

        assertThat(progress.findById(ASSIGNMENT)).get()
                .satisfies(stored -> assertThat(stored.getPickedUpAt()).isNotNull());
    }

    private static ShipmentRouteCourierDTO.ShipmentRouteCourierDTOBuilder assignment() {
        return TestFixtures.assignment(ASSIGNMENT, TestFixtures.DELIVERY_ROUTE_ID);
    }
}

package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.orders.support.TestJwts;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ShipmentRepublishApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRepository shipments;

    @Test
    void adminRepublishesEveryShipment() throws Exception {
        Long first = shipments.save(TestFixtures.pendingShipment()).getId();
        Long second = shipments.save(TestFixtures.pendingShipment()).getId();

        mockMvc.perform(post("/api/shipments/republish").with(testJwts.user("diszpecser@microfutar.hu", "admin")))
                .andExpect(status().isOk());

        await().untilAsserted(() -> assertThat(kafka.received("shipment-topic", ShipmentDTO.class))
                .extracting(ShipmentDTO::getId)
                .containsExactlyInAnyOrder(first, second));
    }

    @Test
    void userWithoutAdminRoleCannotRepublish() throws Exception {
        mockMvc.perform(post("/api/shipments/republish").with(testJwts.user(TestFixtures.SENDER_EMAIL)))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCallerCannotRepublish() throws Exception {
        mockMvc.perform(post("/api/shipments/republish"))
                .andExpect(status().isUnauthorized());
    }
}

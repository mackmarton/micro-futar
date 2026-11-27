package org.bme.micro_futar.courier.controllers;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.repositories.AssignmentProgressRepository;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.KafkaTestClient;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.courier.support.TestJwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class CourierAccessControlApiTests {

    private static final long OTHERS = 3L;

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private CourierScenario scenario;
    @Autowired private AssignmentProgressRepository progress;

    @Test
    void adminRoleOfAnotherPortalGrantsNothing() throws Exception {
        mockMvc.perform(get("/api/package-sizes").with(testJwts.user(TestFixtures.COURIER_EMAIL, "logistics-admin")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "courier-user"})
    void courierAndCommonAdminRoleGrantAccessToPackageSizes(String role) throws Exception {
        mockMvc.perform(get("/api/package-sizes").with(testJwts.user(TestFixtures.COURIER_EMAIL, role)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/pickup/3", "/3/fulfill", "/3/fail",
            "/pickup/999999", "/999999/fulfill", "/999999/fail"
    })
    void actingOnAnAssignmentThatIsNotOwnIsNotFound(String url) throws Exception {
        scenario.publish(TestFixtures.courier());
        scenario.publish(TestFixtures.otherCourier());
        scenario.publish(TestFixtures.deliveryLeg().build());
        scenario.publish(TestFixtures.assignment(OTHERS, TestFixtures.DELIVERY_ROUTE_ID)
                .courierId(TestFixtures.OTHER_COURIER_ID)
                .build());

        mockMvc.perform(post("/api/shipment-route-couriers" + url)
                        .with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isNotFound());

        assertThat(progress.findAll()).isEmpty();
        assertThat(kafka.received("assignment-status-topic")).isEmpty();
        assertThat(kafka.received("shipment-route-fulfilled-topic")).isEmpty();
    }

    @Test
    void unverifiedEmailCannotListAssignments() throws Exception {
        scenario.publish(TestFixtures.courier());

        mockMvc.perform(get("/api/shipment-route-couriers")
                        .with(testJwts.unverifiedUser(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
    }

    @Test
    void unverifiedEmailCannotActOnAnAssignment() throws Exception {
        scenario.publish(TestFixtures.courier());
        scenario.publish(TestFixtures.assignment(1L, TestFixtures.DELIVERY_ROUTE_ID).build());

        mockMvc.perform(post("/api/shipment-route-couriers/pickup/1")
                        .with(testJwts.unverifiedUser(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));

        assertThat(progress.findAll()).isEmpty();
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedBearerTokenIsRejectedWithTheServiceErrorBody() throws Exception {
        // A header instead of TestJwts, so that the real decoder reads the token.
        mockMvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, "Bearer nem-egy-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or malformed access token."))
                .andExpect(jsonPath("$.path").value("/api/me"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "GET, ''",
            "GET, /picked-up-assignments",
            "POST, /pickup-all-deliveries-for-today",
            "POST, /fulfill-all-pickups-for-tocay"
    })
    void callerWithoutCourierRecordGetsNotFoundOnListsAndBulkActions(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), "/api/shipment-route-couriers" + path)
                        .with(testJwts.user("szabo.balint@postafiok.hu", "courier-user")))
                .andExpect(status().isNotFound());
    }
}

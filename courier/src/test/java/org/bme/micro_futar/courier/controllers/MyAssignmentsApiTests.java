package org.bme.micro_futar.courier.controllers;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.courier.support.TestJwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bme.micro_futar.courier.support.CourierScenario.DELIVERY;
import static org.bme.micro_futar.courier.support.CourierScenario.PICKUP;
import static org.bme.micro_futar.courier.support.CourierScenario.delivery;
import static org.bme.micro_futar.courier.support.CourierScenario.pickup;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class MyAssignmentsApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private CourierScenario scenario;

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
    void listsOnlyOwnActiveAssignmentsOfToday() throws Exception {
        scenario.publish(TestFixtures.otherCourier());
        scenario.publish(delivery().id(3L).courierId(TestFixtures.OTHER_COURIER_ID).build());
        scenario.publish(delivery().id(4L).dateAssignedFor(LocalDate.now().minusDays(1)).build());
        scenario.publish(delivery().id(5L).canceled(true).build());

        getAsCourier("/api/me/assignments")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].assignmentId").value(containsInAnyOrder((int) PICKUP, (int) DELIVERY)));
    }

    @Test
    void pickupExposesOnlyTheSenderWithoutEmail() throws Exception {
        String body = getAsCourier("/api/me/assignments/" + PICKUP)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentType").value("PICKUP"))
                .andExpect(jsonPath("$.contactName").value("Kovács Eszter"))
                .andExpect(jsonPath("$.contactPhone").value("+36301847265"))
                .andExpect(jsonPath("$.address").value("Ráday utca 31."))
                .andExpect(jsonPath("$.latitude").value(47.4857))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).doesNotContain("Gruber", "Neubaugasse", "@");
    }

    @Test
    void deliveryExposesOnlyTheRecipient() throws Exception {
        String body = getAsCourier("/api/me/assignments/" + DELIVERY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentType").value("DELIVERY"))
                .andExpect(jsonPath("$.contactName").value("Lukas Gruber"))
                .andExpect(jsonPath("$.contactPhone").value("+436641284573"))
                .andExpect(jsonPath("$.address").value("Neubaugasse 25"))
                .andExpect(jsonPath("$.latitude").value(48.2006))
                .andExpect(jsonPath("$.parcelNumber").value(TestFixtures.PARCEL_NUMBER))
                .andExpect(jsonPath("$.packageSizeId").value(TestFixtures.SMALL_PACKAGE_ID))
                .andExpect(jsonPath("$.senderName").doesNotExist())
                .andExpect(jsonPath("$.senderPhone").doesNotExist())
                .andExpect(jsonPath("$.senderEmail").doesNotExist())
                .andExpect(jsonPath("$.price").doesNotExist())
                .andExpect(jsonPath("$.maxPrice").doesNotExist())
                .andExpect(jsonPath("$.currencyCode").doesNotExist())
                .andExpect(jsonPath("$.paymentStatus").doesNotExist())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).doesNotContain("Eszter", "+36301847265", "@");
    }

    @ParameterizedTest
    @ValueSource(longs = {3L, 4L, 5L, 999999L})
    void assignmentThatIsNotOwnAndActiveIsNotFound(long assignmentId) throws Exception {
        scenario.publish(TestFixtures.otherCourier());
        scenario.publish(delivery().id(3L).courierId(TestFixtures.OTHER_COURIER_ID).build());
        scenario.publish(delivery().id(4L).dateAssignedFor(LocalDate.now().minusDays(1)).build());
        scenario.publish(delivery().id(5L).canceled(true).build());

        getAsCourier("/api/me/assignments/" + assignmentId)
                .andExpect(status().isNotFound());
    }

    @Test
    void assignmentWithUnknownRouteIsListedWithoutDetails() throws Exception {
        scenario.publish(TestFixtures.assignment(6L, 999L).build());

        getAsCourier("/api/me/assignments/6")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentRouteId").value(999))
                .andExpect(jsonPath("$.assignmentType").doesNotExist())
                .andExpect(jsonPath("$.contactName").doesNotExist());
    }

    private ResultActions getAsCourier(String url) throws Exception {
        return mockMvc.perform(get(url).with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")));
    }
}

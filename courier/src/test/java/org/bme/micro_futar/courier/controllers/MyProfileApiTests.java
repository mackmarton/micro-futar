package org.bme.micro_futar.courier.controllers;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.courier.support.TestJwts;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class MyProfileApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private CourierScenario scenario;

    @Test
    void courierSeesTheirOwnProfileWithVehicle() throws Exception {
        scenario.publish(TestFixtures.vehicle());
        scenario.publish(TestFixtures.courier());
        scenario.publish(TestFixtures.otherCourier());

        mockMvc.perform(get("/api/me").with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestFixtures.COURIER_ID))
                .andExpect(jsonPath("$.email").value(TestFixtures.COURIER_EMAIL))
                .andExpect(jsonPath("$.vehicle.registrationNumber").value("AEKR-482"))
                .andExpect(jsonPath("$.vehicle.maximumPackableVolume").value(3_000_000.0));
    }

    @Test
    void courierWithoutVehicleHasNoVehicleInTheProfile() throws Exception {
        scenario.publish(TestFixtures.vehicle());
        scenario.publish(TestFixtures.otherCourier());

        mockMvc.perform(get("/api/me").with(testJwts.user(TestFixtures.OTHER_COURIER_EMAIL, "courier-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestFixtures.OTHER_COURIER_ID))
                .andExpect(jsonPath("$.vehicle").doesNotExist());
    }

    @Test
    void deletedVehicleIsNotShownInTheProfile() throws Exception {
        scenario.publish(TestFixtures.vehicle());
        scenario.publish(TestFixtures.courier());
        VehicleDTO deleted = TestFixtures.vehicle();
        deleted.setDeleted(true);
        scenario.publish(deleted);

        mockMvc.perform(get("/api/me").with(testJwts.user(TestFixtures.COURIER_EMAIL, "courier-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicle").doesNotExist());
    }

    @Test
    void callerWithoutCourierRecordGetsNotFound() throws Exception {
        scenario.publish(TestFixtures.courier());
        var stranger = testJwts.user("szabo.balint@postafiok.hu", "courier-user");

        mockMvc.perform(get("/api/me").with(stranger))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/me/assignments").with(stranger))
                .andExpect(status().isNotFound());
    }

    @Test
    void unverifiedEmailIsForbiddenWithMachineReadableCode() throws Exception {
        scenario.publish(TestFixtures.courier());

        for (String url : new String[]{"/api/me", "/api/me/assignments"}) {
            mockMvc.perform(get(url).with(testJwts.unverifiedUser(TestFixtures.COURIER_EMAIL, "courier-user")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
        }
    }
}

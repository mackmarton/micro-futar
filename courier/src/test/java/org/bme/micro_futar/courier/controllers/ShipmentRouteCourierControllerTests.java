package org.bme.micro_futar.courier.controllers;

import org.bme.micro_futar.courier.EnableTestContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableTestContainers
class ShipmentRouteCourierControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor unverifiedCourier() {
        return jwt()
                .jwt(token -> token.claim("email", "courier@example.com").claim("email_verified", false))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void unverifiedEmailCannotListAssignments() throws Exception {
        mockMvc.perform(get("/api/shipment-route-couriers").with(unverifiedCourier()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
    }

    @Test
    void unverifiedEmailCannotActOnAnAssignment() throws Exception {
        mockMvc.perform(post("/api/shipment-route-couriers/pickup/1").with(unverifiedCourier()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
    }
}

package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.orders.support.TestJwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ShipmentListingApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private ShipmentRepository shipments;

    @Test
    void verifiedSenderSeesOnlyTheirOwnShipments() throws Exception {
        Shipment own = givenShipmentOf(TestFixtures.SENDER_EMAIL);
        givenShipmentOf("szabo.balint@postafiok.hu");

        mockMvc.perform(get("/api/shipments").with(testJwts.user(TestFixtures.SENDER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(own.getId()));
    }

    @Test
    void senderWithoutShipmentsSeesNothingOfOthers() throws Exception {
        givenShipmentOf(TestFixtures.SENDER_EMAIL);

        mockMvc.perform(get("/api/shipments").with(testJwts.user("szabo.balint@postafiok.hu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void unverifiedEmailIsForbiddenWithMachineReadableCode() throws Exception {
        givenShipmentOf(TestFixtures.SENDER_EMAIL);

        mockMvc.perform(get("/api/shipments").with(testJwts.unverifiedUser(TestFixtures.SENDER_EMAIL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
    }

    @Test
    void listingWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedBearerTokenIsRejectedWithTheServiceErrorBody() throws Exception {
        // A header instead of TestJwts, so that the real decoder reads the token.
        mockMvc.perform(get("/api/shipments").header(HttpHeaders.AUTHORIZATION, "Bearer nem-egy-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or malformed access token."))
                .andExpect(jsonPath("$.path").value("/api/shipments"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    private Shipment givenShipmentOf(String senderEmail) {
        Shipment shipment = TestFixtures.pendingShipment();
        shipment.setSenderEmail(senderEmail);
        return shipments.save(shipment);
    }
}

package org.bme.micro_futar.logistics.controllers;

import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.bme.micro_futar.logistics.mappers.ShipmentMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentManualReviewRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRepository;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.support.TestJwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The shipments an administrator has to handle by hand. */
@IntegrationTest
class ShipmentManualReviewApiTests {

    private static final String REASON = "Shipment was canceled after a courier had picked it up";

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private ShipmentManualReviewRepository reviews;
    @Autowired private ShipmentRepository shipments;
    @Autowired private ShipmentMapper shipmentMapper;

    @Test
    void openItemsAreListedForTheAdmin() throws Exception {
        shipments.save(shipmentMapper.toEntity(TestFixtures.shipment(7L).build()));
        givenReview(7L);

        asAdmin(get("/api/shipment-manual-reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].shipmentId").value(7))
                .andExpect(jsonPath("$[0].parcelNumber").value(TestFixtures.PARCEL_NUMBER))
                .andExpect(jsonPath("$[0].recipientAddress").value("Neubaugasse 25"))
                .andExpect(jsonPath("$[0].reason").value(REASON));
    }

    @Test
    void resolvedItemKeepsItsFirstResolver() throws Exception {
        givenReview(7L);

        asAdmin(post("/api/shipment-manual-reviews/{id}/resolve", 7)).andExpect(status().isNoContent());
        ZonedDateTime resolvedAt = reviews.findById(7L).orElseThrow().getResolvedAt();
        mockMvc.perform(post("/api/shipment-manual-reviews/{id}/resolve", 7)
                        .with(testJwts.user(TestFixtures.DISPATCHER_EMAIL, "logistics-user", "admin")))
                .andExpect(status().isNoContent());

        assertThat(resolvedAt).isNotNull();
        assertThat(reviews.findById(7L)).get().satisfies(review -> {
            assertThat(review.getResolvedAt()).isEqualTo(resolvedAt);
            assertThat(review.getResolvedBy()).isEqualTo(TestFixtures.ADMIN_EMAIL);
        });
    }

    @Test
    void resolvedItemsAreListedOnlyOnRequest() throws Exception {
        givenReview(7L);
        asAdmin(post("/api/shipment-manual-reviews/{id}/resolve", 7)).andExpect(status().isNoContent());

        asAdmin(get("/api/shipment-manual-reviews"))
                .andExpect(jsonPath("$").isEmpty());
        asAdmin(get("/api/shipment-manual-reviews").param("includeResolved", "true"))
                .andExpect(jsonPath("$[0].resolvedBy").value(TestFixtures.ADMIN_EMAIL));
    }

    @Test
    void unknownShipmentCannotBeResolved() throws Exception {
        asAdmin(post("/api/shipment-manual-reviews/{id}/resolve", 404_404)).andExpect(status().isNotFound());

        assertThat(reviews.findAll()).isEmpty();
    }

    @Test
    void manualReviewsRequireAdmin() throws Exception {
        givenReview(7L);
        var dispatcher = testJwts.user(TestFixtures.DISPATCHER_EMAIL, "logistics-user");

        mockMvc.perform(get("/api/shipment-manual-reviews").with(dispatcher)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/shipment-manual-reviews/{id}/resolve", 7).with(dispatcher))
                .andExpect(status().isForbidden());

        assertThat(reviews.findById(7L)).get().extracting(ShipmentManualReview::getResolvedAt).isNull();
    }

    private void givenReview(long shipmentId) {
        ShipmentManualReview review = new ShipmentManualReview();
        review.setShipmentId(shipmentId);
        review.setReason(REASON);
        review.setFlaggedAt(ZonedDateTime.now());
        reviews.save(review);
    }

    private ResultActions asAdmin(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.with(testJwts.user(TestFixtures.ADMIN_EMAIL, "logistics-user", "admin")));
    }
}

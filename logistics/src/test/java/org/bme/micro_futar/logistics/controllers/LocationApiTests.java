package org.bme.micro_futar.logistics.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.support.TestJwts;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Regions, countries and cities: logistics owns them and announces every change. */
@IntegrationTest
class LocationApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private LocationRegionRepository regions;
    @Autowired private LocationCountryRepository countries;
    @Autowired private LocationCityRepository cities;

    @Test
    void regionIsStoredAndAnnouncedThroughItsLifecycle() throws Exception {
        Long id = read(send(post("/api/regions"), TestFixtures.centralEurope().build())
                .andExpect(status().isCreated()), LocationRegionDTO.class).getId();
        send(put("/api/regions/{id}", id), TestFixtures.centralEurope().name("Kelet-Közép-Európa").build())
                .andExpect(status().isOk());
        assertThat(regions.findById(id)).get().extracting("name").isEqualTo("Kelet-Közép-Európa");

        send(delete("/api/regions/{id}", id), null).andExpect(status().isNoContent());

        assertThat(regions.findById(id)).isEmpty();
        await().untilAsserted(() -> assertThat(kafka.received("location-region-topic", LocationRegionDTO.class))
                .extracting(LocationRegionDTO::getId, LocationRegionDTO::getName, LocationRegionDTO::isDeleted)
                .containsExactly(
                        tuple(id, "Közép-Európa", false),
                        tuple(id, "Kelet-Közép-Európa", false),
                        tuple(id, "Kelet-Közép-Európa", true)));
    }

    @Test
    void countryIsStoredAndAnnouncedThroughItsLifecycle() throws Exception {
        Long regionId = givenRegion();
        Long id = read(send(post("/api/countries"), TestFixtures.hungary(regionId).isoCode("hu").build())
                .andExpect(status().isCreated()), LocationCountryDTO.class).getId();
        assertThat(countries.findById(id)).get().extracting("isoCode").isEqualTo("HU");

        send(put("/api/countries/{id}", id), TestFixtures.hungary(regionId).name("Magyar Köztársaság").build())
                .andExpect(status().isOk());
        send(delete("/api/countries/{id}", id), null).andExpect(status().isNoContent());

        assertThat(countries.findById(id)).isEmpty();
        await().untilAsserted(() -> assertThat(kafka.received("location-country-topic", LocationCountryDTO.class))
                .extracting(LocationCountryDTO::getName, LocationCountryDTO::isDeleted)
                .containsExactly(
                        tuple("Magyarország", false),
                        tuple("Magyar Köztársaság", false),
                        tuple("Magyar Köztársaság", true)));
    }

    @Test
    void cityIsStoredAndAnnouncedThroughItsLifecycle() throws Exception {
        Long countryId = givenCountry(givenRegion());
        Long id = read(send(post("/api/cities"), TestFixtures.budapest(countryId).build())
                .andExpect(status().isCreated()), LocationCityDTO.class).getId();
        send(put("/api/cities/{id}", id), TestFixtures.budapest(countryId).name("Budaörs").build())
                .andExpect(status().isOk());
        assertThat(cities.findById(id)).get().extracting("name").isEqualTo("Budaörs");

        send(delete("/api/cities/{id}", id), null).andExpect(status().isNoContent());

        assertThat(cities.findById(id)).isEmpty();
        await().untilAsserted(() -> assertThat(kafka.received("location-city-topic", LocationCityDTO.class))
                .extracting(LocationCityDTO::getName, LocationCityDTO::isDeleted)
                .containsExactly(tuple("Budapest", false), tuple("Budaörs", false), tuple("Budaörs", true)));
    }

    @Test
    void countryWithoutCurrencyGetsForint() throws Exception {
        LocationCountryDTO request = TestFixtures.hungary(givenRegion()).currencyCode(null).build();

        Long id = read(send(post("/api/countries"), request).andExpect(status().isCreated()), LocationCountryDTO.class)
                .getId();

        assertThat(countries.findById(id)).get().extracting("currencyCode").isEqualTo("HUF");
    }

    @Test
    void invalidIsoCodeIsRejected() throws Exception {
        send(post("/api/countries"), TestFixtures.hungary(givenRegion()).isoCode("HUN").build())
                .andExpect(status().isBadRequest());

        assertThat(countries.findAll()).isEmpty();
    }

    @Test
    void countryWithUnknownRegionIsNeitherStoredNorAnnounced() throws Exception {
        Long regionId = givenRegion();
        Long id = countries.save(TestFixtures.entity(TestFixtures.hungary(regionId).build())).getId();
        LocationCountryDTO invalid = TestFixtures.austria(regionId + 1_000).build();

        send(post("/api/countries"), invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("regionId"));
        send(put("/api/countries/{id}", id), invalid).andExpect(status().isBadRequest());

        assertThat(countries.findAll()).singleElement().extracting("name").isEqualTo("Magyarország");
        kafkaTemplate.flush();
        assertThat(kafka.received("location-country-topic")).isEmpty();
    }

    @Test
    void cityWithUnknownCountryIsNeitherStoredNorAnnounced() throws Exception {
        Long countryId = givenCountry(givenRegion());
        Long id = cities.save(TestFixtures.entity(TestFixtures.budapest(countryId).build())).getId();
        LocationCityDTO invalid = TestFixtures.vienna(countryId + 1_000).build();

        send(post("/api/cities"), invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("countryId"));
        send(put("/api/cities/{id}", id), invalid).andExpect(status().isBadRequest());

        assertThat(cities.findAll()).singleElement().extracting("name").isEqualTo("Budapest");
        kafkaTemplate.flush();
        assertThat(kafka.received("location-city-topic")).isEmpty();
    }

    @Test
    void updateOfUnknownCountryOrCityIsNotFoundWithoutValidation() throws Exception {
        // Both refer to something that does not exist, which would be a 400 if it were checked.
        send(put("/api/countries/{id}", 404_404), TestFixtures.hungary(505_505L).build())
                .andExpect(status().isNotFound());
        send(put("/api/cities/{id}", 404_404), TestFixtures.budapest(505_505L).build())
                .andExpect(status().isNotFound());
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/regions")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/regions").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userWithoutLogisticsRoleIsForbidden() throws Exception {
        mockMvc.perform(post("/api/regions")
                        .with(testJwts.user(TestFixtures.DISPATCHER_EMAIL, "courier-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestFixtures.centralEurope().build())))
                .andExpect(status().isForbidden());

        assertThat(regions.findAll()).isEmpty();
    }

    private Long givenRegion() {
        return regions.save(TestFixtures.entity(TestFixtures.centralEurope().build())).getId();
    }

    private Long givenCountry(Long regionId) {
        return countries.save(TestFixtures.entity(TestFixtures.hungary(regionId).build())).getId();
    }

    private ResultActions send(MockHttpServletRequestBuilder request, Object body) throws Exception {
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        }
        return mockMvc.perform(request.with(testJwts.user(TestFixtures.DISPATCHER_EMAIL, "logistics-user")));
    }

    private <T> T read(ResultActions result, Class<T> type) throws Exception {
        return objectMapper.readValue(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), type);
    }
}

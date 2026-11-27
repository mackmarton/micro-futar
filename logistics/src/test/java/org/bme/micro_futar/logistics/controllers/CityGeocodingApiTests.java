package org.bme.micro_futar.logistics.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.dtos.GeocodeJobStatusDTO;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.exceptions.GeocodingException;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.logistics.services.geocoding.GeocodeResult;
import org.bme.micro_futar.logistics.services.geocoding.OrsGeocodingClient;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.support.TestJwts;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class CityGeocodingApiTests {

    private static final GeocodeResult BUDAPEST =
            new GeocodeResult(47.4979, 19.0402, 47.3496, 18.9251, 47.6131, 19.3349);

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private LocationRegionRepository regions;
    @Autowired private LocationCountryRepository countries;
    @Autowired private LocationCityRepository cities;
    @Autowired private OrsGeocodingClient geocoder;

    private Long countryId;

    @BeforeEach
    void givenHungary() {
        Long regionId = regions.save(TestFixtures.entity(TestFixtures.centralEurope().build())).getId();
        countryId = countries.save(TestFixtures.entity(TestFixtures.hungary(regionId).build())).getId();
    }

    @Test
    void newCityIsStoredWithGeodataFromTheGeocoder() throws Exception {
        when(geocoder.geocode("Budapest", "HU")).thenReturn(Optional.of(BUDAPEST));

        Long id = create(TestFixtures.budapest(countryId).build());

        assertThat(stored(id)).satisfies(city -> {
            assertThat(city.getCenterLatitude()).isEqualTo(47.4979);
            assertThat(city.getBboxMaxLongitude()).isEqualTo(19.3349);
            assertThat(city.getGeocodedAt()).isNotNull();
            assertThat(city.isGeoManual()).isFalse();
        });
        await().untilAsserted(() -> assertThat(kafka.received("location-city-topic", LocationCityDTO.class))
                .singleElement().extracting(LocationCityDTO::getCenterLatitude).isEqualTo(47.4979));
    }

    @Test
    void cityIsSavedWithoutGeodataWhenTheGeocoderIsDown() throws Exception {
        when(geocoder.geocode(any(), any())).thenThrow(new GeocodingException("timeout", null));
        LocationCityDTO request = TestFixtures.budapest(countryId)
                // the client cannot claim these
                .geocodedAt(ZonedDateTime.now())
                .geoManual(true)
                .build();

        Long id = create(request);

        assertThat(stored(id).getCenterLatitude()).isNull();
        assertThat(stored(id).getGeocodedAt()).isNull();
        assertThat(stored(id).isGeoManual()).isFalse();
    }

    @Test
    void coordinatesSentByTheAdminMakeTheCityManual() throws Exception {
        when(geocoder.geocode(any(), any())).thenReturn(Optional.of(BUDAPEST));

        Long id = create(TestFixtures.budapest(countryId).centerLatitude(47.5).centerLongitude(19.05).build());

        assertThat(stored(id).getCenterLatitude()).isEqualTo(47.5);
        assertThat(stored(id).isGeoManual()).isTrue();
        assertThat(stored(id).getGeocodedAt()).isNull();
    }

    @Test
    void updateWithoutGeodataKeepsTheStoredOne() throws Exception {
        when(geocoder.geocode("Budapest", "HU")).thenReturn(Optional.of(BUDAPEST));
        Long id = create(TestFixtures.budapest(countryId).build());
        ZonedDateTime geocodedAt = stored(id).getGeocodedAt();
        update(id, TestFixtures.budapest(countryId)
                .centerLatitude(47.5).centerLongitude(19.05)
                .bboxMinLatitude(47.35).bboxMinLongitude(18.93)
                .bboxMaxLatitude(47.61).bboxMaxLongitude(19.33)
                .build());

        update(id, TestFixtures.budapest(countryId).name("Budapest főváros").build());

        assertThat(stored(id)).satisfies(city -> {
            assertThat(city.getName()).isEqualTo("Budapest főváros");
            assertThat(city.getCenterLatitude()).isEqualTo(47.5);
            assertThat(city.getBboxMaxLatitude()).isEqualTo(47.61);
            assertThat(city.getGeocodedAt()).isEqualTo(geocodedAt);
            assertThat(city.isGeoManual()).isTrue();
        });
        await().untilAsserted(() -> assertThat(kafka.received("location-city-topic", LocationCityDTO.class))
                .hasSize(3)
                .last().extracting(LocationCityDTO::getCenterLatitude).isEqualTo(47.5));
    }

    @Test
    void bulkGeocodingDoesNotOverwriteAManualCity() throws Exception {
        when(geocoder.geocode("Budapest", "HU")).thenReturn(Optional.of(BUDAPEST));
        Long manual = create(TestFixtures.budapest(countryId).build());
        update(manual, TestFixtures.budapest(countryId).centerLatitude(47.5).centerLongitude(19.05).build());
        when(geocoder.geocode(any(), any())).thenReturn(Optional.of(BUDAPEST));

        runBulkGeocoding();

        assertThat(stored(manual).getCenterLatitude()).isEqualTo(47.5);
        assertThat(stored(manual).isGeoManual()).isTrue();
    }

    @Test
    void missingGeodataIsFilledInTheBackgroundAndTheStatusReportsIt() throws Exception {
        Long szeged = givenCityWithoutGeodata("Szeged");
        givenCityWithoutGeodata("Győr");
        when(geocoder.geocode("Szeged", "HU")).thenReturn(Optional.of(BUDAPEST));
        when(geocoder.geocode("Győr", "HU")).thenReturn(Optional.empty());

        GeocodeJobStatusDTO result = runBulkGeocoding();

        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getGeocoded()).isEqualTo(1);
        assertThat(result.getNotFound()).isEqualTo(1);
        assertThat(stored(szeged).getCenterLatitude()).isEqualTo(47.4979);
        await().untilAsserted(() -> assertThat(kafka.received("location-city-topic", LocationCityDTO.class))
                .extracting(LocationCityDTO::getId).containsExactly(szeged));
    }

    @Test
    void regeocodingReplacesManualGeodata() throws Exception {
        Long id = create(TestFixtures.budapest(countryId).centerLatitude(47.5).centerLongitude(19.05).build());
        when(geocoder.geocode("Budapest", "HU")).thenReturn(Optional.of(BUDAPEST));

        asAdmin(post("/api/cities/{id}/geocode", id)).andExpect(status().isOk());

        assertThat(stored(id).getCenterLatitude()).isEqualTo(47.4979);
        assertThat(stored(id).isGeoManual()).isFalse();
        await().untilAsserted(() -> assertThat(kafka.received("location-city-topic", LocationCityDTO.class))
                .hasSize(2)
                .last().extracting(LocationCityDTO::getCenterLatitude).isEqualTo(47.4979));
    }

    @Test
    void regeocodingWithoutAHitIsReported() throws Exception {
        Long id = givenCityWithoutGeodata("Szeged");

        asAdmin(post("/api/cities/{id}/geocode", id)).andExpect(status().isUnprocessableEntity());
        asAdmin(post("/api/cities/{id}/geocode", 404_404)).andExpect(status().isNotFound());
        when(geocoder.geocode(any(), any())).thenThrow(new GeocodingException("timeout", null));
        asAdmin(post("/api/cities/{id}/geocode", id)).andExpect(status().isBadGateway());
    }

    @Test
    void geocodingEndpointsRequireAdmin() throws Exception {
        var dispatcher = testJwts.user(TestFixtures.DISPATCHER_EMAIL, "logistics-user");

        mockMvc.perform(post("/api/cities/geocode-missing").with(dispatcher)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/cities/geocode-missing/status").with(dispatcher)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/cities/{id}/geocode", 1).with(dispatcher)).andExpect(status().isForbidden());
    }

    private Long givenCityWithoutGeodata(String name) {
        return cities.save(TestFixtures.entity(TestFixtures.budapest(countryId).name(name).build())).getId();
    }

    private LocationCity stored(Long id) {
        return cities.findById(id).orElseThrow();
    }

    private Long create(LocationCityDTO city) throws Exception {
        String body = asAdmin(post("/api/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(city)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(body, LocationCityDTO.class).getId();
    }

    private void update(Long id, LocationCityDTO city) throws Exception {
        asAdmin(put("/api/cities/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(city)))
                .andExpect(status().isOk());
    }

    /** Starts the background run and returns its status once it is done. The job outlives the test's database. */
    private GeocodeJobStatusDTO runBulkGeocoding() throws Exception {
        asAdmin(post("/api/cities/geocode-missing")).andExpect(status().isAccepted());
        await().until(() -> jobStatus().getState() != GeocodeJobStatusDTO.State.RUNNING);
        GeocodeJobStatusDTO result = jobStatus();
        assertThat(result.getState()).isEqualTo(GeocodeJobStatusDTO.State.DONE);
        return result;
    }

    private GeocodeJobStatusDTO jobStatus() throws Exception {
        String body = asAdmin(get("/api/cities/geocode-missing/status"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(body, GeocodeJobStatusDTO.class);
    }

    private ResultActions asAdmin(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.with(testJwts.user(TestFixtures.ADMIN_EMAIL, "logistics-user", "admin")));
    }
}

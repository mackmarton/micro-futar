package org.bme.micro_futar.logistics.services.geocoding;

import org.bme.micro_futar.logistics.exceptions.GeocodingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OrsGeocodingClientTests {

    private static final String BUDAPEST_RESPONSE = """
            {"features":[{"type":"Feature",
              "geometry":{"type":"Point","coordinates":[19.0402,47.4979]},
              "properties":{"name":"Budapest","country_a":"HUN","layer":"locality"},
              "bbox":[18.9,47.35,19.3,47.65]}]}
            """;

    private MockRestServiceServer server;
    private OrsGeocodingClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new OrsGeocodingClient(restTemplate, "secret-key", OrsGeocodingClient.SEARCH_URL);
    }

    @Test
    void sendsLocalityOnlySingleResultRestrictedToTheCountry() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(OrsGeocodingClient.SEARCH_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("text", "Budapest"))
                .andExpect(queryParam("boundary.country", "HU"))
                .andExpect(queryParam("layers", "locality"))
                .andExpect(queryParam("size", "1"))
                .andExpect(header("Authorization", "secret-key"))
                .andRespond(withSuccess(BUDAPEST_RESPONSE, MediaType.APPLICATION_JSON));

        GeocodeResult result = client.geocode("Budapest", "hu").orElseThrow();

        assertThat(result.centerLatitude()).isEqualTo(47.4979);
        assertThat(result.centerLongitude()).isEqualTo(19.0402);
        assertThat(result.bboxMinLatitude()).isEqualTo(47.35);
        assertThat(result.bboxMinLongitude()).isEqualTo(18.9);
        assertThat(result.bboxMaxLatitude()).isEqualTo(47.65);
        assertThat(result.bboxMaxLongitude()).isEqualTo(19.3);
        server.verify();
    }

    @Test
    void encodesCityNamesWithSpacesAndAccents() {
        server.expect(queryParam("text", "Sz%C3%A9kesfeh%C3%A9rv%C3%A1r%20Als%C3%B3v%C3%A1ros"))
                .andRespond(withSuccess("{\"features\":[]}", MediaType.APPLICATION_JSON));

        assertThat(client.geocode("Székesfehérvár Alsóváros", "HU")).isEmpty();
    }

    @Test
    void resultWithoutBboxHasOnlyACentre() {
        GeocodeResult result = OrsGeocodingClient.parse(java.util.Map.of("features", java.util.List.of(java.util.Map.of(
                "geometry", java.util.Map.of("coordinates", java.util.List.of(19.0, 47.5))))), "HU").orElseThrow();
        assertThat(result.hasBbox()).isFalse();
        assertThat(result.centerLatitude()).isEqualTo(47.5);
    }

    @Test
    void ignoresAHitInAnotherCountry() {
        server.expect(method(HttpMethod.GET)).andRespond(withSuccess(BUDAPEST_RESPONSE, MediaType.APPLICATION_JSON));
        assertThat(client.geocode("Budapest", "AT")).isEmpty();
    }

    @Test
    void emptyFeatureListMeansNoResult() {
        server.expect(method(HttpMethod.GET)).andRespond(withSuccess("{\"features\":[]}", MediaType.APPLICATION_JSON));
        assertThat(client.geocode("Nowhere", "HU")).isEmpty();
    }

    @Test
    void serverErrorBecomesGeocodingException() {
        server.expect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThatThrownBy(() -> client.geocode("Budapest", "HU")).isInstanceOf(GeocodingException.class);
    }
}

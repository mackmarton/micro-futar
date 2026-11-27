package org.bme.micro_futar.logistics.services.fetchers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenRouteServiceFetcherTests {

    private static final String MATRIX_URL = "https://api.openrouteservice.org/v2/matrix/driving-car";
    private static final double[] SENDER = {19.0658, 47.4857};
    private static final double[] BUDAPEST_DEPO = {19.0891, 47.4639};
    private static final double[] GYOR_DEPO = {17.6504, 47.6875};

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    private final OpenRouteServiceFetcher fetcher = new OpenRouteServiceFetcher(restTemplate);

    @BeforeEach
    void setApiKey() {
        ReflectionTestUtils.setField(fetcher, "apiKey", "test-key");
    }

    @Test
    void picksTheDestinationWithTheShortestDrive() {
        server.expect(requestTo(MATRIX_URL))
                .andExpect(header("Authorization", "test-key"))
                // the origin is the first location, the depos are the destinations
                .andExpect(jsonPath("$.locations[0][0]").value(19.0658))
                .andExpect(jsonPath("$.destinations.length()").value(2))
                .andRespond(withSuccess("{\"durations\":[[5400.0,420.0]]}", MediaType.APPLICATION_JSON));

        int closest = fetcher.getClosestIndexByDuration(SENDER, List.of(GYOR_DEPO, BUDAPEST_DEPO));

        assertThat(closest).isEqualTo(1);
        server.verify();
    }

    @Test
    void singleDestinationNeedsNoRequest() {
        assertThat(fetcher.getClosestIndexByDuration(SENDER, List.of(BUDAPEST_DEPO))).isZero();

        server.verify();
    }

    @Test
    void responseWithoutDurationsFails() {
        server.expect(requestTo(MATRIX_URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fetcher.getClosestIndexByDuration(SENDER, List.of(GYOR_DEPO, BUDAPEST_DEPO)))
                .isInstanceOf(IllegalStateException.class);
    }
}

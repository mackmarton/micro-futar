package org.bme.micro_futar.api_gateway.csrf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class GatewayCsrfHeaderTests extends AbstractGatewayRedisTest {

    private static final String HEADER = "X-Requested-With";
    private static final String PATH = "/api/orders/anything";

    @Test
    void postWithoutTheHeaderIsRejectedWithAReadableError() {
        expectRejected(client.post().uri(PATH).header(HttpHeaders.ORIGIN, UI_ORIGIN).exchange())
                // A felület csak CORS-fejlécekkel tudja elolvasni a hibakódot.
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, UI_ORIGIN);
    }

    @Test
    void postWithAWrongHeaderValueIsRejected() {
        expectRejected(client.post().uri(PATH).header(HEADER, "XMLHttpRequest").exchange());
    }

    // Origin nélkül, mert a PATCH-et a CORS-beállítás amúgy sem engedi: így biztosan a fejlécszűrő utasít el.
    @ParameterizedTest
    @ValueSource(strings = {"PUT", "PATCH", "DELETE"})
    void otherStateChangingMethodsNeedTheHeaderToo(String method) {
        expectRejected(client.method(HttpMethod.valueOf(method)).uri(PATH).exchange());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/orders/x", "/api/logistics/x", "/api/courier/x", "/api/payments/checkout",
            "/api/tracking/x", "/api/payments/webhooks/stripe/other"})
    void everyApiPathIsProtected(String path) {
        expectRejected(client.post().uri(path).exchange());
    }

    @Test
    void postWithTheHeaderPasses() {
        client.post().uri(PATH).header(HEADER, "micro-futar").header(HttpHeaders.ORIGIN, UI_ORIGIN).exchange()
                .expectStatus().isOk();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "HEAD"})
    void safeMethodsPassWithoutTheHeader(String method) {
        client.method(HttpMethod.valueOf(method)).uri(PATH).exchange().expectStatus().isOk();
    }

    @Test
    void stripeWebhookPassesWithoutTheHeader() {
        client.post().uri("/api/payments/webhooks/stripe").exchange().expectStatus().isOk();
    }

    @Test
    void preflightAllowsTheCustomHeader() {
        client.options().uri(PATH)
                .header(HttpHeaders.ORIGIN, UI_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,x-requested-with")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, UI_ORIGIN)
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "content-type, x-requested-with");
    }

    private WebTestClient.ResponseSpec expectRejected(WebTestClient.ResponseSpec response) {
        response.expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().jsonPath("$.code").isEqualTo("CSRF_HEADER_MISSING");
        return response;
    }
}

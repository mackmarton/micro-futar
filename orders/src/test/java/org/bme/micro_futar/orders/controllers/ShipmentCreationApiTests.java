package org.bme.micro_futar.orders.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.dtos.CreateShipmentResponseDTO;
import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.orders.support.TestJwts;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.bme.micro_futar.shared.utils.PaymentTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ShipmentCreationApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRepository shipments;

    @BeforeEach
    void givenCitiesAndPrice() {
        kafka.sendAndAwaitConsumption("location-city-topic", "1", TestFixtures.budapest().build());
        kafka.sendAndAwaitConsumption("location-city-topic", "2", TestFixtures.vienna().build());
        givenPrice(TestFixtures.countryPrice().build());
    }

    @Test
    void guestCreatesShipmentAndReceivesPaymentToken() throws Exception {
        CreateShipmentResponseDTO response = create(TestFixtures.shipmentRequest().build());

        assertThat(response.getPaymentToken()).isNotBlank();
        assertThat(shipments.findAll()).singleElement().satisfies(shipment -> {
            assertThat(shipment.getId()).isEqualTo(response.getShipment().getId());
            assertThat(shipment.getParcelNumber()).isNotBlank();
            assertThat(shipment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(shipment.isConfirmed()).isFalse();
        });
    }

    @Test
    void priceSentByTheClientIsIgnored() throws Exception {
        ShipmentDTO request = TestFixtures.shipmentRequest()
                .price(1.0)
                .maxPrice(1.0)
                .currencyCode("EUR")
                .paymentStatus(PaymentStatus.PAID)
                .confirmed(true)
                .build();

        mockMvc.perform(post("/api/shipments/new")
                        .with(testJwts.user(TestFixtures.SENDER_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(shipments.findAll()).singleElement().satisfies(shipment -> {
            assertThat(shipment.getPrice()).isNull();
            assertThat(shipment.getMaxPrice()).isEqualTo(4990.0);
            assertThat(shipment.getCurrencyCode()).isEqualTo("HUF");
            assertThat(shipment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(shipment.isConfirmed()).isFalse();
        });
    }

    @Test
    void maximumPriceComesFromThePriceListOfTheRoute() throws Exception {
        givenPrice(TestFixtures.countryPrice()
                .id(2L)
                .originCountryId(TestFixtures.AUSTRIA_ID)
                .destinationCountryId(TestFixtures.HUNGARY_ID)
                .maxPrice(6490.0)
                .build());
        givenPrice(TestFixtures.countryPrice().id(3L).packageSizeId(2L).maxPrice(7990.0).build());

        create(TestFixtures.shipmentRequest().packageSizeId(2L).build());

        assertThat(shipments.findAll()).singleElement()
                .extracting(Shipment::getMaxPrice).isEqualTo(7990.0);
    }

    @Test
    void paymentIsRequestedForTheNewShipment() throws Exception {
        CreateShipmentResponseDTO response = create(TestFixtures.shipmentRequest().build());

        await().untilAsserted(() ->
                assertThat(kafka.received("payment-request-topic", PaymentRequestedEventDTO.class))
                        .singleElement()
                        .satisfies(payment -> {
                            assertThat(payment.getShipmentId()).isEqualTo(response.getShipment().getId());
                            assertThat(payment.getMaxAmount()).isEqualByComparingTo("4990");
                            assertThat(payment.getCurrencyCode()).isEqualTo("HUF");
                            assertThat(payment.getSenderEmail()).isEqualTo(TestFixtures.SENDER_EMAIL);
                            assertThat(payment.getPaymentTokenHash())
                                    .isEqualTo(PaymentTokens.hash(response.getPaymentToken()));
                        }));
    }

    @Test
    void shipmentIsNotAnnouncedBeforeThePaymentIsAuthorized() throws Exception {
        create(TestFixtures.shipmentRequest().build());

        await().untilAsserted(() -> assertThat(kafka.received("payment-request-topic")).hasSize(1));
        assertThat(kafka.received("shipment-topic")).isEmpty();
        assertThat(kafka.received("shipment-notification-topic")).isEmpty();
    }

    @Test
    void routeWithoutPriceIsRejected() throws Exception {
        ShipmentDTO request = TestFixtures.shipmentRequest().packageSizeId(2L).build();

        submit(request).andExpect(status().isUnprocessableEntity());

        assertThat(shipments.findAll()).isEmpty();
    }

    @Test
    void senderLocationOutsideTheCityIsRejected() throws Exception {
        // Debrecen, while the sender city is Budapest
        ShipmentDTO request = TestFixtures.shipmentRequest()
                .senderLatitude(47.5316)
                .senderLongitude(21.6273)
                .build();

        submit(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_TOO_FAR_FROM_CITY"))
                .andExpect(jsonPath("$.party").value("SENDER"));

        assertThat(shipments.findAll()).isEmpty();
    }

    @Test
    void recipientLocationOutsideTheCityIsRejected() throws Exception {
        // Graz, while the recipient city is Vienna
        ShipmentDTO request = TestFixtures.shipmentRequest()
                .recipientLatitude(47.0707)
                .recipientLongitude(15.4395)
                .build();

        submit(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_TOO_FAR_FROM_CITY"))
                .andExpect(jsonPath("$.party").value("RECIPIENT"));

        assertThat(shipments.findAll()).isEmpty();
    }

    private void givenPrice(CountryPriceDTO price) {
        kafka.sendAndAwaitConsumption("country-price-topic", price.getId().toString(), price);
    }

    /** Submits the request as a guest, without a token. */
    private ResultActions submit(ShipmentDTO request) throws Exception {
        return mockMvc.perform(post("/api/shipments/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private CreateShipmentResponseDTO create(ShipmentDTO request) throws Exception {
        String body = submit(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(body, CreateShipmentResponseDTO.class);
    }
}

package org.bme.micro_futar.tracking.controllers;

import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.tracking.IntegrationTest;
import org.bme.micro_futar.tracking.support.KafkaTestClient;
import org.bme.micro_futar.tracking.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ParcelTrackingApiTests {

    private static final ZonedDateTime PICKED_UP_AT = ZonedDateTime.parse("2026-03-14T09:26:00+01:00");
    private static final ZonedDateTime ARRIVED_IN_VIENNA_AT = ZonedDateTime.parse("2026-03-14T17:05:00+01:00");
    private static final ZonedDateTime DELIVERED_AT = ZonedDateTime.parse("2026-03-15T11:40:00+01:00");

    @Autowired private MockMvc mockMvc;
    @Autowired private KafkaTestClient kafka;

    @BeforeEach
    void givenNetworkAndShipment() {
        publish(TestFixtures.hungary().build());
        publish(TestFixtures.austria().build());
        publish(TestFixtures.budapest().build());
        publish(TestFixtures.vienna().build());
        publish(TestFixtures.budapestDepo());
        publish(TestFixtures.viennaDepo());
        publish(TestFixtures.shipment().build());
    }

    @Test
    void fulfilledLegsAreListedWithPlaceAndFulfillmentTime() throws Exception {
        publish(TestFixtures.pickupLeg().fulfillmentTime(PICKED_UP_AT).build());
        publish(TestFixtures.transitLeg().fulfillmentTime(ARRIVED_IN_VIENNA_AT).build());
        publish(TestFixtures.deliveryLeg().fulfillmentTime(DELIVERED_AT).build());

        track()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canceled").value(false))
                .andExpect(jsonPath("$.trackingParts.length()").value(3))
                .andExpect(jsonPath("$.trackingParts['1'].place").value("Budapest, Magyarország"))
                .andExpect(jsonPath("$.trackingParts['1'].time").value("2026-03-14T08:26:00Z"))
                .andExpect(jsonPath("$.trackingParts['1'].destination").value(false))
                .andExpect(jsonPath("$.trackingParts['2'].place").value("Bécs, Ausztria"))
                .andExpect(jsonPath("$.trackingParts['2'].time").value("2026-03-14T16:05:00Z"))
                .andExpect(jsonPath("$.trackingParts['2'].destination").value(false))
                .andExpect(jsonPath("$.trackingParts['3'].place").value(TestFixtures.DELIVERY_ADDRESS))
                .andExpect(jsonPath("$.trackingParts['3'].time").value("2026-03-15T10:40:00Z"))
                .andExpect(jsonPath("$.trackingParts['3'].destination").value(true));
    }

    @Test
    void legsFollowTheRouteOrderEvenWhenTheirMessagesArriveOutOfOrder() throws Exception {
        publish(TestFixtures.deliveryLeg().fulfillmentTime(DELIVERED_AT).build());
        publish(TestFixtures.transitLeg().fulfillmentTime(ARRIVED_IN_VIENNA_AT).build());
        publish(TestFixtures.pickupLeg().fulfillmentTime(PICKED_UP_AT).build());

        track()
                .andExpect(jsonPath("$.trackingParts['1'].place").value("Budapest, Magyarország"))
                .andExpect(jsonPath("$.trackingParts['2'].place").value("Bécs, Ausztria"))
                .andExpect(jsonPath("$.trackingParts['3'].place").value(TestFixtures.DELIVERY_ADDRESS));
    }

    @Test
    void unfulfilledLegsAreNotListed() throws Exception {
        publish(TestFixtures.pickupLeg().fulfillmentTime(PICKED_UP_AT).build());
        publish(TestFixtures.transitLeg().build());
        publish(TestFixtures.deliveryLeg().build());

        track()
                .andExpect(jsonPath("$.trackingParts.length()").value(1))
                .andExpect(jsonPath("$.trackingParts['1'].place").value("Budapest, Magyarország"));
    }

    @Test
    void parcelWithoutFulfilledLegsHasAnEmptyTimeline() throws Exception {
        publish(TestFixtures.pickupLeg().build());

        track()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingParts").isEmpty());
    }

    @Test
    void legsOfOtherParcelsAreNotListed() throws Exception {
        publish(TestFixtures.pickupLeg().fulfillmentTime(PICKED_UP_AT).build());
        publish(TestFixtures.deliveryLeg()
                .id(203L)
                .shipmentId(TestFixtures.SHIPMENT_ID + 1)
                .fulfillmentTime(DELIVERED_AT)
                .build());

        track()
                .andExpect(jsonPath("$.trackingParts.length()").value(1))
                .andExpect(jsonPath("$.trackingParts['1'].place").value("Budapest, Magyarország"));
    }

    @Test
    void placeNamesFollowTheLocalCopies() throws Exception {
        publish(TestFixtures.pickupLeg().fulfillmentTime(PICKED_UP_AT).build());

        publish(TestFixtures.budapest().name("Budapest XIX. kerület").build());
        publish(TestFixtures.hungary().name("Hungary").build());

        track().andExpect(jsonPath("$.trackingParts['1'].place").value("Budapest XIX. kerület, Hungary"));
    }

    @Test
    void deletedDepotCityAndCountryStillNamePastLegs() throws Exception {
        publish(TestFixtures.pickupLeg().fulfillmentTime(PICKED_UP_AT).build());
        DepoDTO closedDepot = TestFixtures.budapestDepo();
        closedDepot.setDeleted(true);

        publish(closedDepot);
        publish(TestFixtures.budapest().deleted(true).build());
        publish(TestFixtures.hungary().deleted(true).build());

        track()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingParts['1'].place").value("Budapest, Magyarország"));
    }

    @Test
    void legToADepotThatHasNotArrivedYetIsListedWithoutPlace() throws Exception {
        publish(TestFixtures.pickupLeg().destinationDepoId(99L).fulfillmentTime(PICKED_UP_AT).build());
        publish(TestFixtures.transitLeg().fulfillmentTime(ARRIVED_IN_VIENNA_AT).build());

        track()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingParts['1'].place").isEmpty())
                .andExpect(jsonPath("$.trackingParts['1'].time").value("2026-03-14T08:26:00Z"))
                .andExpect(jsonPath("$.trackingParts['2'].place").value("Bécs, Ausztria"));
    }

    @Test
    void depotWhoseCityOrCountryHasNotArrivedYetGivesNoPlace() throws Exception {
        DepoDTO inUnknownCity = TestFixtures.budapestDepo();
        inUnknownCity.setLocationCityId(99L);
        DepoDTO inUnknownCountry = TestFixtures.viennaDepo();
        inUnknownCountry.setLocationCountryId(99L);
        publish(inUnknownCity);
        publish(inUnknownCountry);

        publish(TestFixtures.pickupLeg().fulfillmentTime(PICKED_UP_AT).build());
        publish(TestFixtures.transitLeg().fulfillmentTime(ARRIVED_IN_VIENNA_AT).build());

        track()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingParts.length()").value(2))
                .andExpect(jsonPath("$.trackingParts['1'].place").isEmpty())
                .andExpect(jsonPath("$.trackingParts['2'].place").isEmpty());
    }

    @Test
    void canceledParcelIsReportedAsCanceled() throws Exception {
        publish(TestFixtures.pickupLeg().build());

        publish(TestFixtures.shipment().canceled(true).build());
        publish(TestFixtures.pickupLeg().canceled(true).build());

        track()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canceled").value(true))
                .andExpect(jsonPath("$.trackingParts").isEmpty());
    }

    @Test
    void unknownParcelNumberIsNotFound() throws Exception {
        mockMvc.perform(get("/api/track/{parcelNumber}", "0b9d4f17-8a2c-4c61-b3e5-7f1a6d2c9e08"))
                .andExpect(status().isNotFound());
    }

    private ResultActions track() throws Exception {
        return mockMvc.perform(get("/api/track/{parcelNumber}", TestFixtures.PARCEL_NUMBER));
    }

    private void publish(LocationCountryDTO country) {
        kafka.sendAndAwaitConsumption("location-country-topic", country.getId().toString(), country);
    }

    private void publish(LocationCityDTO city) {
        kafka.sendAndAwaitConsumption("location-city-topic", city.getId().toString(), city);
    }

    private void publish(DepoDTO depot) {
        kafka.sendAndAwaitConsumption("depo-topic", depot.getId().toString(), depot);
    }

    private void publish(ShipmentDTO shipment) {
        kafka.sendAndAwaitConsumption("shipment-topic", shipment.getId().toString(), shipment);
    }

    private void publish(ShipmentRouteDTO leg) {
        kafka.sendAndAwaitConsumption("shipment-route-topic", leg.getId().toString(), leg);
    }
}

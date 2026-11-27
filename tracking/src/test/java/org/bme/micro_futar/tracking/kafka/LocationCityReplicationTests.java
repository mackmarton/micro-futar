package org.bme.micro_futar.tracking.kafka;

import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.tracking.IntegrationTest;
import org.bme.micro_futar.tracking.entities.LocationCity;
import org.bme.micro_futar.tracking.repositories.LocationCityRepository;
import org.bme.micro_futar.tracking.support.KafkaTestClient;
import org.bme.micro_futar.tracking.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class LocationCityReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private LocationCityRepository cities;

    @Test
    void newCityIsStored() {
        publish(TestFixtures.budapest().build());

        assertThat(cities.findById(TestFixtures.BUDAPEST_ID)).get().satisfies(city -> {
            assertThat(city.getName()).isEqualTo("Budapest");
            assertThat(city.getCountryId()).isEqualTo(TestFixtures.HUNGARY_ID);
        });
    }

    @Test
    void changedCityOverwritesTheLocalCopy() {
        publish(TestFixtures.vienna().name("Wien").build());

        publish(TestFixtures.vienna().build());

        assertThat(cities.findAll()).singleElement()
                .extracting(LocationCity::getName).isEqualTo("Bécs");
    }

    @Test
    void deletedCityIsSoftDeleted() {
        publish(TestFixtures.budapest().build());

        publish(TestFixtures.budapest().deleted(true).build());

        assertThat(cities.findAll()).isEmpty();
        assertThat(cities.findById(TestFixtures.BUDAPEST_ID)).get()
                .extracting(LocationCity::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredCityKeepsASingleCopy() {
        publish(TestFixtures.budapest().build());

        publish(TestFixtures.budapest().build());

        assertThat(cities.findAll()).singleElement()
                .extracting(LocationCity::getName).isEqualTo("Budapest");
    }

    /** Logistics publishes geo data on the city topic; tracking does not store it but must not choke on it. */
    @Test
    void cityMessageWithGeoAndUnknownFieldsIsStillProcessed() {
        kafka.sendAndAwaitConsumption("location-city-topic", "1", """
                {"id":1,"countryId":1,"name":"Budapest","deleted":false,
                 "centerLatitude":47.4979,"centerLongitude":19.0402,
                 "bboxMinLatitude":47.3496,"bboxMinLongitude":18.9251,
                 "bboxMaxLatitude":47.6131,"bboxMaxLongitude":19.3349,
                 "geocodedAt":"2026-03-14T10:26:00+01:00","geoManual":true,
                 "someFutureField":"x"}
                """);

        assertThat(cities.findById(TestFixtures.BUDAPEST_ID)).get()
                .extracting(LocationCity::getName).isEqualTo("Budapest");
    }

    private void publish(LocationCityDTO message) {
        kafka.sendAndAwaitConsumption("location-city-topic", message.getId().toString(), message);
    }
}

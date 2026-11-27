package org.bme.micro_futar.orders.kafka;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.orders.repositories.LocationCityRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

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
            assertThat(city.getCenterLatitude()).isEqualTo(47.4979);
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
    void updatedCityTakesOverItsGeoData() {
        publish(TestFixtures.budapest()
                .centerLatitude(null)
                .centerLongitude(null)
                .bboxMinLatitude(null)
                .bboxMinLongitude(null)
                .bboxMaxLatitude(null)
                .bboxMaxLongitude(null)
                .build());
        ZonedDateTime geocodedAt = ZonedDateTime.parse("2026-03-14T09:26:00Z");

        publish(TestFixtures.budapest().geocodedAt(geocodedAt).geoManual(true).build());

        assertThat(cities.findById(TestFixtures.BUDAPEST_ID)).get().satisfies(city -> {
            assertThat(city.getCenterLatitude()).isEqualTo(47.4979);
            assertThat(city.getCenterLongitude()).isEqualTo(19.0402);
            assertThat(city.getBboxMinLatitude()).isEqualTo(47.3496);
            assertThat(city.getBboxMinLongitude()).isEqualTo(18.9251);
            assertThat(city.getBboxMaxLatitude()).isEqualTo(47.6131);
            assertThat(city.getBboxMaxLongitude()).isEqualTo(19.3349);
            assertThat(city.getGeocodedAt().toInstant()).isEqualTo(geocodedAt.toInstant());
            assertThat(city.isGeoManual()).isTrue();
        });
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

    private void publish(LocationCityDTO message) {
        kafka.sendAndAwaitConsumption("location-city-topic", message.getId().toString(), message);
    }
}

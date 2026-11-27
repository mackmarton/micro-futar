package org.bme.micro_futar.orders.kafka;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.LocationCountry;
import org.bme.micro_futar.orders.repositories.LocationCountryRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class LocationCountryReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private LocationCountryRepository countries;

    @Test
    void newCountryIsStored() {
        publish(TestFixtures.austria().build());

        assertThat(countries.findById(TestFixtures.AUSTRIA_ID)).get().satisfies(country -> {
            assertThat(country.getName()).isEqualTo("Ausztria");
            assertThat(country.getRegionId()).isEqualTo(TestFixtures.CENTRAL_EUROPE_ID);
            assertThat(country.getCurrencyCode()).isEqualTo("EUR");
        });
    }

    @Test
    void changedCountryOverwritesTheLocalCopy() {
        publish(TestFixtures.hungary().build());

        publish(TestFixtures.hungary().regionId(2L).currencyCode("EUR").build());

        assertThat(countries.findAll()).singleElement().satisfies(country -> {
            assertThat(country.getRegionId()).isEqualTo(2L);
            assertThat(country.getCurrencyCode()).isEqualTo("EUR");
        });
    }

    @Test
    void deletedCountryIsSoftDeleted() {
        publish(TestFixtures.hungary().build());

        publish(TestFixtures.hungary().deleted(true).build());

        assertThat(countries.findAll()).isEmpty();
        assertThat(countries.findById(TestFixtures.HUNGARY_ID)).get()
                .extracting(LocationCountry::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredCountryKeepsASingleCopy() {
        publish(TestFixtures.hungary().build());

        publish(TestFixtures.hungary().build());

        assertThat(countries.findAll()).singleElement()
                .extracting(LocationCountry::getName).isEqualTo("Magyarország");
    }

    private void publish(LocationCountryDTO message) {
        kafka.sendAndAwaitConsumption("location-country-topic", message.getId().toString(), message);
    }
}

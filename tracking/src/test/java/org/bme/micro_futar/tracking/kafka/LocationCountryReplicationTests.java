package org.bme.micro_futar.tracking.kafka;

import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.tracking.IntegrationTest;
import org.bme.micro_futar.tracking.entities.LocationCountry;
import org.bme.micro_futar.tracking.repositories.LocationCountryRepository;
import org.bme.micro_futar.tracking.support.KafkaTestClient;
import org.bme.micro_futar.tracking.support.TestFixtures;
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
            assertThat(country.getCurrencyCode()).isEqualTo("EUR");
        });
    }

    @Test
    void changedCountryOverwritesTheLocalCopy() {
        publish(TestFixtures.austria().name("Österreich").build());

        publish(TestFixtures.austria().build());

        assertThat(countries.findAll()).singleElement()
                .extracting(LocationCountry::getName).isEqualTo("Ausztria");
    }

    @Test
    void deletedCountryIsSoftDeleted() {
        publish(TestFixtures.austria().build());

        publish(TestFixtures.austria().deleted(true).build());

        assertThat(countries.findAll()).isEmpty();
        assertThat(countries.findById(TestFixtures.AUSTRIA_ID)).get()
                .extracting(LocationCountry::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredCountryKeepsASingleCopy() {
        publish(TestFixtures.austria().build());

        publish(TestFixtures.austria().build());

        assertThat(countries.findAll()).singleElement()
                .extracting(LocationCountry::getName).isEqualTo("Ausztria");
    }

    private void publish(LocationCountryDTO message) {
        kafka.sendAndAwaitConsumption("location-country-topic", message.getId().toString(), message);
    }
}

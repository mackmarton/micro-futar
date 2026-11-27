package org.bme.micro_futar.orders.kafka;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.LocationRegion;
import org.bme.micro_futar.orders.repositories.LocationRegionRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class LocationRegionReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private LocationRegionRepository regions;

    @Test
    void newRegionIsStored() {
        publish(TestFixtures.centralEurope().build());

        assertThat(regions.findById(TestFixtures.CENTRAL_EUROPE_ID)).get()
                .extracting(LocationRegion::getName).isEqualTo("Közép-Európa");
    }

    @Test
    void changedRegionOverwritesTheLocalCopy() {
        publish(TestFixtures.centralEurope().build());

        publish(TestFixtures.centralEurope().name("Közép-Kelet-Európa").build());

        assertThat(regions.findAll()).singleElement()
                .extracting(LocationRegion::getName).isEqualTo("Közép-Kelet-Európa");
    }

    @Test
    void deletedRegionIsSoftDeleted() {
        publish(TestFixtures.centralEurope().build());

        publish(TestFixtures.centralEurope().deleted(true).build());

        assertThat(regions.findAll()).isEmpty();
        assertThat(regions.findById(TestFixtures.CENTRAL_EUROPE_ID)).get()
                .extracting(LocationRegion::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredRegionKeepsASingleCopy() {
        publish(TestFixtures.centralEurope().build());

        publish(TestFixtures.centralEurope().build());

        assertThat(regions.findAll()).singleElement()
                .extracting(LocationRegion::getName).isEqualTo("Közép-Európa");
    }

    private void publish(LocationRegionDTO message) {
        kafka.sendAndAwaitConsumption("location-region-topic", message.getId().toString(), message);
    }
}

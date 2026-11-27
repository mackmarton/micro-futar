package org.bme.micro_futar.tracking.kafka;

import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.tracking.IntegrationTest;
import org.bme.micro_futar.tracking.entities.Depo;
import org.bme.micro_futar.tracking.repositories.DepoRepository;
import org.bme.micro_futar.tracking.support.KafkaTestClient;
import org.bme.micro_futar.tracking.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class DepoReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private DepoRepository depos;

    @Test
    void newDepotIsStored() {
        publish(TestFixtures.budapestDepo());

        assertThat(depos.findById(TestFixtures.BUDAPEST_DEPO_ID)).get().satisfies(depot -> {
            assertThat(depot.getName()).isEqualTo("Budapest Dél depó");
            assertThat(depot.getLocationCityId()).isEqualTo(TestFixtures.BUDAPEST_ID);
            assertThat(depot.getLocationCountryId()).isEqualTo(TestFixtures.HUNGARY_ID);
        });
    }

    @Test
    void changedDepotOverwritesTheLocalCopy() {
        publish(TestFixtures.budapestDepo());
        DepoDTO moved = TestFixtures.budapestDepo();
        moved.setAddress("Soroksári út 164.");

        publish(moved);

        assertThat(depos.findAll()).singleElement()
                .extracting(Depo::getAddress).isEqualTo("Soroksári út 164.");
    }

    @Test
    void deletedDepotIsSoftDeleted() {
        publish(TestFixtures.budapestDepo());
        DepoDTO deleted = TestFixtures.budapestDepo();
        deleted.setDeleted(true);

        publish(deleted);

        assertThat(depos.findAll()).isEmpty();
        assertThat(depos.findById(TestFixtures.BUDAPEST_DEPO_ID)).get()
                .extracting(Depo::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredDepotKeepsASingleCopy() {
        publish(TestFixtures.budapestDepo());

        publish(TestFixtures.budapestDepo());

        assertThat(depos.findAll()).singleElement()
                .extracting(Depo::getName).isEqualTo("Budapest Dél depó");
    }

    private void publish(DepoDTO message) {
        kafka.sendAndAwaitConsumption("depo-topic", message.getId().toString(), message);
    }
}

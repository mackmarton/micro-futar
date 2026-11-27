package org.bme.micro_futar.courier.kafka;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.entities.Depo;
import org.bme.micro_futar.courier.repositories.DepoRepository;
import org.bme.micro_futar.courier.support.KafkaTestClient;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class DepoReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private DepoRepository depos;

    @Test
    void newDepoIsStored() {
        publish(TestFixtures.budapestDepo());

        assertThat(depos.findById(TestFixtures.BUDAPEST_DEPO_ID)).get().satisfies(depo -> {
            assertThat(depo.getName()).isEqualTo("Budapest Dél depó");
            assertThat(depo.getAddress()).isEqualTo("Gubacsi út 6.");
        });
    }

    @Test
    void changedDepoOverwritesTheLocalCopy() {
        publish(TestFixtures.budapestDepo());
        DepoDTO changed = TestFixtures.budapestDepo();
        changed.setAddress("Soroksári út 110.");

        publish(changed);

        assertThat(depos.findAll()).singleElement()
                .extracting(Depo::getAddress).isEqualTo("Soroksári út 110.");
    }

    @Test
    void deletedDepoIsSoftDeleted() {
        publish(TestFixtures.budapestDepo());
        DepoDTO deleted = TestFixtures.budapestDepo();
        deleted.setDeleted(true);

        publish(deleted);

        assertThat(depos.findAll()).isEmpty();
        assertThat(depos.findById(TestFixtures.BUDAPEST_DEPO_ID)).get()
                .extracting(Depo::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredDepoKeepsASingleCopy() {
        publish(TestFixtures.budapestDepo());

        publish(TestFixtures.budapestDepo());

        assertThat(depos.findAll()).singleElement()
                .extracting(Depo::getAddress).isEqualTo("Gubacsi út 6.");
    }

    private void publish(DepoDTO message) {
        kafka.sendAndAwaitConsumption("depo-topic", message.getId().toString(), message);
    }
}

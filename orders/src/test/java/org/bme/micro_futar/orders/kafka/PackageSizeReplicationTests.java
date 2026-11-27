package org.bme.micro_futar.orders.kafka;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.PackageSize;
import org.bme.micro_futar.orders.repositories.PackageSizeRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class PackageSizeReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private PackageSizeRepository packageSizes;

    @Test
    void newPackageSizeIsStored() {
        publish(TestFixtures.smallPackage().build());

        assertThat(packageSizes.findById(TestFixtures.SMALL_PACKAGE_ID)).get().satisfies(size -> {
            assertThat(size.getName()).isEqualTo("S");
            assertThat(size.getMaxLength()).isEqualTo(35.0);
        });
    }

    @Test
    void changedPackageSizeOverwritesTheLocalCopy() {
        publish(TestFixtures.smallPackage().build());

        publish(TestFixtures.smallPackage().maxLength(40.0).build());

        assertThat(packageSizes.findAll()).singleElement()
                .extracting(PackageSize::getMaxLength).isEqualTo(40.0);
    }

    @Test
    void deletedPackageSizeIsSoftDeleted() {
        publish(TestFixtures.smallPackage().build());

        publish(TestFixtures.smallPackage().deleted(true).build());

        assertThat(packageSizes.findAll()).isEmpty();
        assertThat(packageSizes.findById(TestFixtures.SMALL_PACKAGE_ID)).get()
                .extracting(PackageSize::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredPackageSizeKeepsASingleCopy() {
        publish(TestFixtures.smallPackage().build());

        publish(TestFixtures.smallPackage().build());

        assertThat(packageSizes.findAll()).singleElement()
                .extracting(PackageSize::getName).isEqualTo("S");
    }

    private void publish(PackageSizeDTO message) {
        kafka.sendAndAwaitConsumption("package-size-topic", message.getId().toString(), message);
    }
}

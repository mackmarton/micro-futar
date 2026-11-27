package org.bme.micro_futar.courier.kafka;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.entities.Shipment;
import org.bme.micro_futar.courier.repositories.ShipmentRepository;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ShipmentReplicationTests {

    @Autowired private CourierScenario scenario;
    @Autowired private ShipmentRepository shipments;

    @Test
    void newShipmentIsStored() {
        scenario.publish(TestFixtures.shipment().build());

        assertThat(shipments.findById(TestFixtures.SHIPMENT_ID)).get().satisfies(shipment -> {
            assertThat(shipment.getSenderName()).isEqualTo("Kovács Eszter");
            assertThat(shipment.getRecipientAddress()).isEqualTo("Neubaugasse 25");
            assertThat(shipment.getPackageSizeId()).isEqualTo(TestFixtures.SMALL_PACKAGE_ID);
            assertThat(shipment.isCanceled()).isFalse();
        });
    }

    @Test
    void changedShipmentOverwritesTheLocalCopy() {
        scenario.publish(TestFixtures.shipment().build());

        scenario.publish(TestFixtures.shipment().price(4290.0).build());

        assertThat(shipments.findAll()).singleElement()
                .extracting(Shipment::getPrice).isEqualTo(4290.0);
    }

    @Test
    void canceledShipmentIsMarkedInTheLocalCopy() {
        scenario.publish(TestFixtures.shipment().build());

        scenario.publish(TestFixtures.shipment().confirmed(false).canceled(true).build());

        assertThat(shipments.findById(TestFixtures.SHIPMENT_ID)).get()
                .extracting(Shipment::isCanceled).isEqualTo(true);
    }

    @Test
    void redeliveredShipmentKeepsASingleCopy() {
        scenario.publish(TestFixtures.shipment().build());

        scenario.publish(TestFixtures.shipment().build());

        assertThat(shipments.findAll()).singleElement()
                .extracting(Shipment::getRecipientName).isEqualTo("Lukas Gruber");
    }
}

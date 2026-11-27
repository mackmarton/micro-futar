package org.bme.micro_futar.tracking.kafka;

import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.tracking.IntegrationTest;
import org.bme.micro_futar.tracking.entities.Shipment;
import org.bme.micro_futar.tracking.repositories.ShipmentRepository;
import org.bme.micro_futar.tracking.support.KafkaTestClient;
import org.bme.micro_futar.tracking.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ShipmentReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRepository shipments;

    @Test
    void newShipmentIsStored() {
        publish(TestFixtures.shipment().build());

        assertThat(shipments.findByParcelNumber(TestFixtures.PARCEL_NUMBER)).get().satisfies(shipment -> {
            assertThat(shipment.getId()).isEqualTo(TestFixtures.SHIPMENT_ID);
            assertThat(shipment.getSenderName()).isEqualTo("Kovács Eszter");
            assertThat(shipment.getRecipientName()).isEqualTo("Lukas Gruber");
            assertThat(shipment.isCanceled()).isFalse();
        });
    }

    @Test
    void changedShipmentOverwritesTheLocalCopy() {
        publish(TestFixtures.shipment().build());

        publish(TestFixtures.shipment().delivered(true).price(4290.0).build());

        assertThat(shipments.findAll()).singleElement().satisfies(shipment -> {
            assertThat(shipment.isDelivered()).isTrue();
            assertThat(shipment.getPrice()).isEqualTo(4290.0);
        });
    }

    @Test
    void canceledShipmentIsMarkedInTheLocalCopy() {
        publish(TestFixtures.shipment().build());

        publish(TestFixtures.shipment().canceled(true).build());

        assertThat(shipments.findAll()).singleElement().extracting(Shipment::isCanceled).isEqualTo(true);
    }

    @Test
    void redeliveredShipmentKeepsASingleCopy() {
        publish(TestFixtures.shipment().build());

        publish(TestFixtures.shipment().build());

        assertThat(shipments.findAll()).singleElement()
                .extracting(Shipment::getParcelNumber).isEqualTo(TestFixtures.PARCEL_NUMBER);
    }

    private void publish(ShipmentDTO message) {
        kafka.sendAndAwaitConsumption("shipment-topic", message.getId().toString(), message);
    }
}

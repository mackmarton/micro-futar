package org.bme.micro_futar.orders.kafka;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDeliveredEventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ShipmentDeliveryReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRepository shipments;

    @Test
    void deliveredShipmentIsMarkedAndRepublished() {
        Long id = shipments.save(TestFixtures.pendingShipment()).getId();

        publishDelivery(id);

        assertThat(shipments.findById(id)).get().extracting(Shipment::isDelivered).isEqualTo(true);
        assertThat(kafka.received("shipment-topic", ShipmentDTO.class)).singleElement()
                .satisfies(shipment -> assertThat(shipment.isDelivered()).isTrue());
    }

    @Test
    void redeliveredDeliveryIsRepublishedOnlyOnce() {
        Long id = shipments.save(TestFixtures.pendingShipment()).getId();

        publishDelivery(id);
        publishDelivery(id);

        assertThat(kafka.received("shipment-topic")).hasSize(1);
    }

    @Test
    void deliveryOfUnknownShipmentDoesNotBlockLaterOnes() {
        Long id = shipments.save(TestFixtures.pendingShipment()).getId();

        publishDelivery(id + 1000);
        publishDelivery(id);

        assertThat(shipments.findAll()).singleElement().extracting(Shipment::isDelivered).isEqualTo(true);
        assertThat(kafka.received("shipment-topic")).hasSize(1);
    }

    private void publishDelivery(Long shipmentId) {
        var event = ShipmentDeliveredEventDTO.builder()
                .shipmentId(shipmentId)
                .deliveredAt(ZonedDateTime.now())
                .build();
        kafka.sendAndAwaitConsumption("shipment-delivered-topic", shipmentId.toString(), event);
    }
}

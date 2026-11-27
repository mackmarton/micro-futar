package org.bme.micro_futar.tracking.kafka;

import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.tracking.IntegrationTest;
import org.bme.micro_futar.tracking.entities.ShipmentRoute;
import org.bme.micro_futar.tracking.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.tracking.support.KafkaTestClient;
import org.bme.micro_futar.tracking.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ShipmentRouteReplicationTests {

    private static final long LEG_ID = 101L;

    @Autowired private KafkaTestClient kafka;
    @Autowired private ShipmentRouteRepository routes;

    @Test
    void newLegIsStored() {
        publish(TestFixtures.pickupLeg().build());

        assertThat(routes.findById(LEG_ID)).get().satisfies(leg -> {
            assertThat(leg.getShipmentId()).isEqualTo(TestFixtures.SHIPMENT_ID);
            assertThat(leg.getDestinationDepoId()).isEqualTo(TestFixtures.BUDAPEST_DEPO_ID);
            assertThat(leg.getRoutePartNumber()).isZero();
            assertThat(leg.getFulfillmentTime()).isNull();
        });
    }

    @Test
    void fulfilledLegTakesOverItsFulfillmentTime() {
        publish(TestFixtures.pickupLeg().build());
        ZonedDateTime fulfilledAt = ZonedDateTime.parse("2026-03-14T09:26:00Z");

        publish(TestFixtures.pickupLeg().fulfillmentTime(fulfilledAt).build());

        assertThat(routes.findAll()).singleElement()
                .satisfies(leg -> assertThat(leg.getFulfillmentTime().toInstant()).isEqualTo(fulfilledAt.toInstant()));
    }

    @Test
    void canceledLegIsMarked() {
        publish(TestFixtures.pickupLeg().build());

        publish(TestFixtures.pickupLeg().canceled(true).build());

        assertThat(routes.findAll()).singleElement().extracting(ShipmentRoute::isCanceled).isEqualTo(true);
    }

    @Test
    void redeliveredLegKeepsASingleCopy() {
        publish(TestFixtures.pickupLeg().build());

        publish(TestFixtures.pickupLeg().build());

        assertThat(routes.findAll()).singleElement().extracting(ShipmentRoute::getId).isEqualTo(LEG_ID);
    }

    private void publish(ShipmentRouteDTO message) {
        kafka.sendAndAwaitConsumption("shipment-route-topic", message.getId().toString(), message);
    }
}

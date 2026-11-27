package org.bme.micro_futar.courier.kafka;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.entities.ShipmentRoute;
import org.bme.micro_futar.courier.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ShipmentRouteReplicationTests {

    @Autowired private CourierScenario scenario;
    @Autowired private ShipmentRouteRepository routes;

    @Test
    void newRouteLegIsStored() {
        scenario.publish(TestFixtures.deliveryLeg().build());

        assertThat(routes.findById(TestFixtures.DELIVERY_ROUTE_ID)).get().satisfies(route -> {
            assertThat(route.getShipmentId()).isEqualTo(TestFixtures.SHIPMENT_ID);
            assertThat(route.getDestinationAddress()).isEqualTo("Neubaugasse 25");
            assertThat(route.getOriginAddress()).isNull();
        });
    }

    @Test
    void changedRouteLegOverwritesTheLocalCopy() {
        scenario.publish(TestFixtures.deliveryLeg().build());

        scenario.publish(TestFixtures.deliveryLeg().destinationAddress("Lindengasse 12").build());

        assertThat(routes.findAll()).singleElement()
                .extracting(ShipmentRoute::getDestinationAddress).isEqualTo("Lindengasse 12");
    }

    @Test
    void canceledRouteLegIsMarkedInTheLocalCopy() {
        scenario.publish(TestFixtures.deliveryLeg().build());

        scenario.publish(TestFixtures.deliveryLeg().canceled(true).build());

        assertThat(routes.findById(TestFixtures.DELIVERY_ROUTE_ID)).get()
                .extracting(ShipmentRoute::isCanceled).isEqualTo(true);
    }

    @Test
    void redeliveredRouteLegKeepsASingleCopy() {
        scenario.publish(TestFixtures.deliveryLeg().build());

        scenario.publish(TestFixtures.deliveryLeg().build());

        assertThat(routes.findAll()).singleElement()
                .extracting(ShipmentRoute::getRoutePartNumber).isEqualTo(2);
    }
}

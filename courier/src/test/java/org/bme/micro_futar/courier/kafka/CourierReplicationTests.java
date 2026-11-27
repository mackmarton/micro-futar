package org.bme.micro_futar.courier.kafka;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.entities.Courier;
import org.bme.micro_futar.courier.repositories.CourierRepository;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class CourierReplicationTests {

    @Autowired private CourierScenario scenario;
    @Autowired private CourierRepository couriers;

    @Test
    void newCourierIsStored() {
        scenario.publish(TestFixtures.courier());

        assertThat(couriers.findById(TestFixtures.COURIER_ID)).get().satisfies(courier -> {
            assertThat(courier.getName()).isEqualTo("Tóth Gábor");
            assertThat(courier.getEmail()).isEqualTo(TestFixtures.COURIER_EMAIL);
            assertThat(courier.getVehicleId()).isEqualTo(TestFixtures.VEHICLE_ID);
        });
    }

    @Test
    void changedCourierOverwritesTheLocalCopy() {
        scenario.publish(TestFixtures.courier());
        CourierDTO changed = TestFixtures.courier();
        changed.setTelephone("+36304471852");

        scenario.publish(changed);

        assertThat(couriers.findAll()).singleElement()
                .extracting(Courier::getTelephone).isEqualTo("+36304471852");
    }

    @Test
    void deletedCourierIsSoftDeleted() {
        scenario.publish(TestFixtures.courier());
        CourierDTO deleted = TestFixtures.courier();
        deleted.setDeleted(true);

        scenario.publish(deleted);

        assertThat(couriers.findAll()).isEmpty();
        assertThat(couriers.findByEmail(TestFixtures.COURIER_EMAIL)).isEmpty();
        assertThat(couriers.findById(TestFixtures.COURIER_ID)).get()
                .extracting(Courier::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredCourierKeepsASingleCopy() {
        scenario.publish(TestFixtures.courier());

        scenario.publish(TestFixtures.courier());

        assertThat(couriers.findAll()).singleElement()
                .extracting(Courier::getTelephone).isEqualTo("+36209184736");
    }
}

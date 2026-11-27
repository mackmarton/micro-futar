package org.bme.micro_futar.courier.kafka;

import org.bme.micro_futar.courier.IntegrationTest;
import org.bme.micro_futar.courier.entities.Vehicle;
import org.bme.micro_futar.courier.repositories.VehicleRepository;
import org.bme.micro_futar.courier.support.CourierScenario;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class VehicleReplicationTests {

    @Autowired private CourierScenario scenario;
    @Autowired private VehicleRepository vehicles;

    @Test
    void newVehicleIsStored() {
        scenario.publish(TestFixtures.vehicle());

        assertThat(vehicles.findById(TestFixtures.VEHICLE_ID)).get().satisfies(vehicle -> {
            assertThat(vehicle.getRegistrationNumber()).isEqualTo("AEKR-482");
            assertThat(vehicle.getMaximumPackableVolume()).isEqualTo(3_000_000.0);
        });
    }

    @Test
    void changedVehicleOverwritesTheLocalCopy() {
        scenario.publish(TestFixtures.vehicle());
        VehicleDTO changed = TestFixtures.vehicle();
        changed.setMaximumPackableVolume(4_500_000.0);

        scenario.publish(changed);

        assertThat(vehicles.findAll()).singleElement()
                .extracting(Vehicle::getMaximumPackableVolume).isEqualTo(4_500_000.0);
    }

    @Test
    void deletedVehicleIsSoftDeleted() {
        scenario.publish(TestFixtures.vehicle());
        VehicleDTO deleted = TestFixtures.vehicle();
        deleted.setDeleted(true);

        scenario.publish(deleted);

        assertThat(vehicles.findAll()).isEmpty();
        assertThat(vehicles.findById(TestFixtures.VEHICLE_ID)).get()
                .extracting(Vehicle::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredVehicleKeepsASingleCopy() {
        scenario.publish(TestFixtures.vehicle());

        scenario.publish(TestFixtures.vehicle());

        assertThat(vehicles.findAll()).singleElement()
                .extracting(Vehicle::getMaximumPackableVolume).isEqualTo(3_000_000.0);
    }
}

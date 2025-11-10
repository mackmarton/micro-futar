package org.bme.micro_futar.courier.repositories;

import org.bme.micro_futar.courier.entities.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}


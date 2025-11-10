package org.bme.micro_futar.repositories;

import org.bme.micro_futar.entities.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}


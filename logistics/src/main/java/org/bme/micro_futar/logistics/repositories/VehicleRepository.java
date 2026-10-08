package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted rows
    @Query(value = "SELECT * FROM vehicle", nativeQuery = true)
    List<Vehicle> findAllIncludingDeleted();
}

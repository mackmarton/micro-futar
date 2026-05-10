package org.bme.micro_futar.courier.repositories;

import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShipmentRouteCourierRepository extends JpaRepository<ShipmentRouteCourier, Long> {
    List<ShipmentRouteCourier> findAllByCourierIdAndDateAssignedFor(Long courierId, LocalDate now);
}


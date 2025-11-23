package org.bme.micro_futar.courier.repositories;

import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRouteCourierRepository extends JpaRepository<ShipmentRouteCourier, Long> {
    Optional<ShipmentRouteCourier> findByCourierId(Long courierId);
}


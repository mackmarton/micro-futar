package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRouteRepository extends JpaRepository<ShipmentRoute, Long> {
}

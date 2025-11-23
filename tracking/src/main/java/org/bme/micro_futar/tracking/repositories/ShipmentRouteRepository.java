package org.bme.micro_futar.tracking.repositories;

import org.bme.micro_futar.tracking.entities.ShipmentRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRouteRepository extends JpaRepository<ShipmentRoute, Long> {
    List<ShipmentRoute> findAllByShipmentIdEqualsAndFulfillmentTimeIsNotNull(long shipmentId);
}


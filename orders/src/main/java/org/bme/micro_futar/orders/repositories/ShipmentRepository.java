package org.bme.micro_futar.orders.repositories;

import org.bme.micro_futar.orders.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {
}

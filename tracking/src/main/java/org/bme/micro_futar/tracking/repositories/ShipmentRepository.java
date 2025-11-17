package org.bme.micro_futar.tracking.repositories;

import org.bme.micro_futar.tracking.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}


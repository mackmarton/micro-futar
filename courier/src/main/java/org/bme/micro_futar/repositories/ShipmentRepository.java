package org.bme.micro_futar.repositories;

import org.bme.micro_futar.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}


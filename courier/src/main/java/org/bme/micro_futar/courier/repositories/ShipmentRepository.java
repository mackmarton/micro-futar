package org.bme.micro_futar.courier.repositories;

import org.bme.micro_futar.courier.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}


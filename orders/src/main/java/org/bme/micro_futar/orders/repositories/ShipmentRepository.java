package org.bme.micro_futar.orders.repositories;

import org.bme.micro_futar.orders.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {
	List<Shipment> findBySenderEmail(String senderEmail);
}

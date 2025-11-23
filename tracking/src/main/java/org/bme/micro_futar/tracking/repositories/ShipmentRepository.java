package org.bme.micro_futar.tracking.repositories;

import org.bme.micro_futar.tracking.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByParcelNumber(String parcelNumber);
}


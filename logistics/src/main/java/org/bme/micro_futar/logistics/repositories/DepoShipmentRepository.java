package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.DepoShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepoShipmentRepository extends JpaRepository<DepoShipment, Long> {
}

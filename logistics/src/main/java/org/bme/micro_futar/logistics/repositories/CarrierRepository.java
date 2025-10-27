package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {
}


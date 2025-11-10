package org.bme.micro_futar.repositories;

import org.bme.micro_futar.entities.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Long> {
}


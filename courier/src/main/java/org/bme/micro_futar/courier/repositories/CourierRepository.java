package org.bme.micro_futar.courier.repositories;

import org.bme.micro_futar.courier.entities.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Long> {
}


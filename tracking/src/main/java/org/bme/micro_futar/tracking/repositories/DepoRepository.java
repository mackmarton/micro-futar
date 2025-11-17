package org.bme.micro_futar.tracking.repositories;

import org.bme.micro_futar.tracking.entities.Depo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepoRepository extends JpaRepository<Depo, Long> {
}
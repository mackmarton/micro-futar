package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.DepoTransit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepoTransitRepository extends JpaRepository<DepoTransit, Long> {
    List<DepoTransit> findAllByOriginDepoId(Long originDepoId);
    List<DepoTransit> findAllByDestinationDepoId(Long destinationDepoId);
}


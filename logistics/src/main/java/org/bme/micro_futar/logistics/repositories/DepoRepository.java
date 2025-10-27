package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Depo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepoRepository extends JpaRepository<Depo, Long> {
    List<Depo> findAllByLocationCountryId(Long countryId);
}


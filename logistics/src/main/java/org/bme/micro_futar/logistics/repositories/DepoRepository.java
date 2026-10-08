package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Depo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepoRepository extends JpaRepository<Depo, Long> {
    List<Depo> findAllByLocationCountryId(Long countryId);

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted depos
    @Query(value = "SELECT * FROM depo", nativeQuery = true)
    List<Depo> findAllIncludingDeleted();
}


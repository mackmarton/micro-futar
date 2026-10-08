package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationCountryRepository extends JpaRepository<LocationCountry, Long> {
    List<LocationCountry> findAllByRegionId(Long regionId);

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted rows
    @Query(value = "SELECT * FROM location_country", nativeQuery = true)
    List<LocationCountry> findAllIncludingDeleted();
}

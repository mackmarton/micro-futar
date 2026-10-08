package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.LocationRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationRegionRepository extends JpaRepository<LocationRegion, Long> {

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted rows
    @Query(value = "SELECT * FROM location_region", nativeQuery = true)
    List<LocationRegion> findAllIncludingDeleted();
}

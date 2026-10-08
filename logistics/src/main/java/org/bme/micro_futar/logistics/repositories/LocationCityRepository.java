package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.LocationCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationCityRepository extends JpaRepository<LocationCity, Long> {
    List<LocationCity> findAllByCountryId(Long countryId);

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted rows
    @Query(value = "SELECT * FROM location_city", nativeQuery = true)
    List<LocationCity> findAllIncludingDeleted();
}

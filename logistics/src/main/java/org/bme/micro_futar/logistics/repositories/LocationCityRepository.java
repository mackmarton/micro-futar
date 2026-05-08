package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.LocationCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationCityRepository extends JpaRepository<LocationCity, Long> {
    List<LocationCity> findAllByCountryId(Long countryId);
}

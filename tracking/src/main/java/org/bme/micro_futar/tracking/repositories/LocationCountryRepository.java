package org.bme.micro_futar.tracking.repositories;

import org.bme.micro_futar.tracking.entities.LocationCountry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationCountryRepository extends JpaRepository<LocationCountry, Long> {
}

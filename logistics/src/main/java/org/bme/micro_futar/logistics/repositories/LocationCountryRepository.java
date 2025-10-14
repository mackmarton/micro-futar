package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationCountryRepository extends JpaRepository<LocationCountry, Long> {
}

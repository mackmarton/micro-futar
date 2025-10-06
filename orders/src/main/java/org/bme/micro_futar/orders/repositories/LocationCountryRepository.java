package org.bme.micro_futar.orders.repositories;

import org.bme.micro_futar.orders.entities.LocationCountry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationCountryRepository extends JpaRepository<LocationCountry, Long> {
}

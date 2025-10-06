package org.bme.micro_futar.orders.repositories;

import org.bme.micro_futar.orders.entities.LocationRegion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRegionRepository extends JpaRepository<LocationRegion, Long> {
}

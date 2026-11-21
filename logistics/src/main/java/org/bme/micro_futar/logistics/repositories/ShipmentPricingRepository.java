package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.ShipmentPricing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentPricingRepository extends JpaRepository<ShipmentPricing, Long> {
}

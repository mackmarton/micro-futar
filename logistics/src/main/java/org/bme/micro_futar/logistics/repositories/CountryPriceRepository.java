package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryPriceRepository extends JpaRepository<CountryPrice, Long> {
}


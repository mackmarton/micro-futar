package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryPriceRepository extends JpaRepository<CountryPrice, Long> {
    Optional<CountryPrice> findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(Long locationCountryId, Long destinationCountryId, Long packageSizeId);
}


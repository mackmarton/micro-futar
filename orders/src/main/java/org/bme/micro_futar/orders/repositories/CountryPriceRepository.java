package org.bme.micro_futar.orders.repositories;

import org.bme.micro_futar.orders.entities.CountryPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryPriceRepository extends JpaRepository<CountryPrice, Long> {
    Optional<CountryPrice> findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(long originCountryId, long destinationCountryId, long packageSizeId);

    List<CountryPrice> findAllByOriginCountryIdAndDestinationCountryId(Long originCountryId, Long destinationCountryId);
}


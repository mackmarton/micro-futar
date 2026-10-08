package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CountryPriceRepository extends JpaRepository<CountryPrice, Long> {
    Optional<CountryPrice> findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(Long locationCountryId, Long destinationCountryId, Long packageSizeId);

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted rows
    @Query(value = "SELECT * FROM country_price", nativeQuery = true)
    List<CountryPrice> findAllIncludingDeleted();
}

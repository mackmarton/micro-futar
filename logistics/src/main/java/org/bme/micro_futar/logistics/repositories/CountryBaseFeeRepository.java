package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryBaseFeeRepository extends JpaRepository<CountryBaseFee, Long> {
    Optional<CountryBaseFee> findByCountryIdAndPackageSizeId(Long countryId, Long packageSizeId);
}

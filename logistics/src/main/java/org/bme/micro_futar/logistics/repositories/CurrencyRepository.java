package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    boolean existsByCode(String code);
    Optional<Currency> findByCode(String code);
}

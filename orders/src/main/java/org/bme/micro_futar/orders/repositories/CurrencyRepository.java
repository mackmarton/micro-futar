package org.bme.micro_futar.orders.repositories;

import org.bme.micro_futar.orders.entities.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    boolean existsByCode(String code);
}

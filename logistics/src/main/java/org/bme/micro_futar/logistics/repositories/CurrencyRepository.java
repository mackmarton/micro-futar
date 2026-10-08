package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    boolean existsByCode(String code);

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted rows
    @Query(value = "SELECT * FROM currency", nativeQuery = true)
    List<Currency> findAllIncludingDeleted();

    // code is unique across deleted rows too, so these bypass @SQLRestriction
    @Query(value = "SELECT id FROM currency WHERE code = :code", nativeQuery = true)
    Optional<Long> findIdByCodeIncludingDeleted(@Param("code") String code);

    @Modifying
    @Query(value = "UPDATE currency SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreById(@Param("id") Long id);
}

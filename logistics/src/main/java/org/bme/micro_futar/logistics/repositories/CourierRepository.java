package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Courier;
import org.bme.micro_futar.shared.enums.CourierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    List<Courier> findByDepoIdAndCourierType(Long depoId, CourierType courierType);

    List<Courier> findAllByDepoId(Long depoId);

    List<Courier> findAllByDepoIdIsNull();

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted rows
    @Query(value = "SELECT * FROM courier", nativeQuery = true)
    List<Courier> findAllIncludingDeleted();
}

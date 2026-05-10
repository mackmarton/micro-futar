package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShipmentRouteCourierRepository extends JpaRepository<ShipmentRouteCourier, Long> {

    @Query("SELECT COUNT(src) FROM ShipmentRouteCourier src WHERE src.courierId = :courierId AND src.dateAssignedFor = :date")
    long countByCourierIdAndDate(@Param("courierId") Long courierId, @Param("date") LocalDate date);

    List<ShipmentRouteCourier> findByCourierIdAndDateAssignedFor(Long courierId, LocalDate date);
}


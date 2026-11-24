package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ShipmentRouteCourierRepository extends JpaRepository<ShipmentRouteCourier, Long> {

    @Query("SELECT COUNT(src) FROM ShipmentRouteCourier src WHERE src.courierId = :courierId AND src.dateAssignedFor = :date " +
            "AND src.canceled = false AND COALESCE(src.failed, false) = false")
    long countByCourierIdAndDate(@Param("courierId") Long courierId, @Param("date") LocalDate date);

    List<ShipmentRouteCourier> findByCourierIdAndDateAssignedFor(Long courierId, LocalDate date);

    /** Assignments for the date that still count against the courier's capacity (not failed, not canceled). */
    @Query("SELECT src FROM ShipmentRouteCourier src WHERE src.courierId = :courierId AND src.dateAssignedFor = :date " +
            "AND src.canceled = false AND COALESCE(src.failed, false) = false")
    List<ShipmentRouteCourier> findActiveByCourierIdAndDate(@Param("courierId") Long courierId, @Param("date") LocalDate date);

    /** Past-day assignments that were never picked up, failed or fulfilled, so the leg can be assigned again. */
    @Query("SELECT src FROM ShipmentRouteCourier src WHERE src.dateAssignedFor < :today " +
            "AND src.canceled = false AND COALESCE(src.failed, false) = false " +
            "AND COALESCE(src.pickedUpForDelivery, false) = false " +
            "AND EXISTS (SELECT 1 FROM ShipmentRoute r WHERE r.id = src.shipmentRouteId AND r.fulfillmentTime IS NULL)")
    List<ShipmentRouteCourier> findStaleUnfinished(@Param("today") LocalDate today);

    List<ShipmentRouteCourier> findByShipmentRouteIdIn(Collection<Long> shipmentRouteIds);
}


package org.bme.micro_futar.courier.repositories;

import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ShipmentRouteCourierRepository extends JpaRepository<ShipmentRouteCourier, Long> {

    List<ShipmentRouteCourier> findAllByCourierIdAndDateAssignedFor(Long courierId, LocalDate now);

    @Query("SELECT c FROM ShipmentRouteCourier c " +
            "INNER JOIN ShipmentRoute r ON c.shipmentRouteId = r.id " +
            "WHERE c.courierId = :courierId " +
            "AND c.dateAssignedFor = :now " +
            "AND r.destinationAddress IS NOT NULL")
    List<ShipmentRouteCourier> findAllDeliveriesByCourierIdAndDateAssignedFor(Long courierId, LocalDate now);

    @Query("SELECT c FROM ShipmentRouteCourier c " +
            "INNER JOIN ShipmentRoute r ON c.shipmentRouteId = r.id " +
            "WHERE c.courierId = :courierId " +
            "AND c.dateAssignedFor = :now " +
            "AND c.pickedUpForDelivery = true " +
            "AND r.originAddress IS NOT NULL")
    List<ShipmentRouteCourier> findAllPickedUpParcelsByCourierIdAndDateAssignedFor(Long courierId, LocalDate now);
}


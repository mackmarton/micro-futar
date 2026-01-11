package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShipmentRouteRepository extends JpaRepository<ShipmentRoute, Long> {

    @Query("SELECT sr FROM ShipmentRoute sr WHERE sr.originDepoId = :depoId AND sr.destinationAddress IS NOT NULL AND sr.fulfillmentTime IS NULL")
    List<ShipmentRoute> findDeliveryRoutes(@Param("depoId") Long depoId);

    @Query("SELECT sr FROM ShipmentRoute sr WHERE sr.destinationDepoId = :depoId AND sr.originAddress IS NOT NULL AND sr.routePartNumber = 0 AND sr.fulfillmentTime IS NULL")
    List<ShipmentRoute> findPickupRoutes(@Param("depoId") Long depoId);

    @Query("SELECT r1 FROM ShipmentRoute r1 " +
            "WHERE r1.originDepoId = :depoId " +
            "AND r1.destinationDepoId IS NOT NULL " +
            "AND r1.fulfillmentTime IS NULL " +
            "AND  r1.routePartNumber > 0"+
            "AND EXISTS (" +
            "    SELECT r2 FROM ShipmentRoute r2 " +
            "    WHERE r2.shipmentId = r1.shipmentId " +
            "    AND r2.routePartNumber = r1.routePartNumber - 1 " +
            "    AND r2.fulfillmentTime IS NOT NULL" +
            ")")
    List<ShipmentRoute> findCrossDepoRoutesForDepo(@Param("depoId") Long depoId);
}

package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.ShipmentRouteCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface ShipmentRouteCarrierRepository extends JpaRepository<ShipmentRouteCarrier, Long> {

    @Query("SELECT COUNT(src) FROM ShipmentRouteCarrier src WHERE src.carrierId = :carrierId AND src.dateAssignedFor = :date")
    long countByCarrierIdAndDate(@Param("carrierId") Long carrierId, @Param("date") Date date);

    List<ShipmentRouteCarrier> findByCarrierIdAndDateAssignedFor(Long carrierId, Date date);
}


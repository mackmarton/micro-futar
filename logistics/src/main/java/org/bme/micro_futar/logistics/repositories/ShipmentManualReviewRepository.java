package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.ShipmentManualReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentManualReviewRepository extends JpaRepository<ShipmentManualReview, Long> {

    List<ShipmentManualReview> findAllByOrderByFlaggedAtDesc();

    List<ShipmentManualReview> findAllByResolvedAtIsNullOrderByFlaggedAtDesc();
}

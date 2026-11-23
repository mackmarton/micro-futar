package org.bme.micro_futar.logistics.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * A canceled shipment that logistics could not cancel automatically (already picked up or delivered), so an
 * administrator has to deal with it. Owned by logistics, kept apart from the read-only {@link Shipment} copy so
 * that incoming shipment messages never overwrite it.
 */
@Data
@Entity
@NoArgsConstructor
public class ShipmentManualReview {
    @Id
    private Long shipmentId;
    private String reason;
    private ZonedDateTime flaggedAt;
}

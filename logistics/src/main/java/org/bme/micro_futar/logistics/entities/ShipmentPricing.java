package org.bme.micro_futar.logistics.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * The final price logistics computed for a shipment. Owned by logistics, kept apart from the read-only
 * {@link Shipment} copy so that incoming shipment messages never overwrite it.
 */
@Data
@Entity
@NoArgsConstructor
public class ShipmentPricing {
    @Id
    private Long shipmentId;
    private BigDecimal finalPrice;
    private String currencyCode;
    private ZonedDateTime pricedAt;
}

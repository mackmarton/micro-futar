package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Published by courier once the last leg of a shipment was handed over to the recipient. Orders owns the
 * Shipment, so it consumes this, marks the shipment delivered and republishes it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDeliveredEventDTO {
    private Long shipmentId;
    private ZonedDateTime deliveredAt;
}

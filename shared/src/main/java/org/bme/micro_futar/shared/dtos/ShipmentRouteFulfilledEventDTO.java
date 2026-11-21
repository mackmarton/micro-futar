package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Published by courier when a courier completed one leg of a shipment's route. Logistics owns the route, so it
 * consumes this, sets the fulfillment time and republishes the ShipmentRoute.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRouteFulfilledEventDTO {
    private Long shipmentRouteId;
    private Long shipmentId;
    private ZonedDateTime fulfilledAt;
}

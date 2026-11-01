package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Published by logistics once the final price of a shipment is known (after route planning). Payments captures
 * this amount from the authorization; orders and tracking store it as the shipment's price.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentPricedEventDTO {
    private Long shipmentId;
    private BigDecimal finalAmount;
    private String currencyCode;
    private ZonedDateTime pricedAt;
}

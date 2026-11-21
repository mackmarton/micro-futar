package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

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

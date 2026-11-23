package org.bme.micro_futar.logistics.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentManualReviewDTO {
    private Long shipmentId;
    private String parcelNumber;
    private String recipientAddress;
    private boolean delivered;
    private String reason;
    private ZonedDateTime flaggedAt;
    private ZonedDateTime resolvedAt;
    private String resolvedBy;
}

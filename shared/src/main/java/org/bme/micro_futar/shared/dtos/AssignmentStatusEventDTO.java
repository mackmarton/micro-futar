package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.AssignmentStatus;

import java.time.ZonedDateTime;

/**
 * Published by courier when a courier picked up or failed an assignment. Logistics owns the assignment, so it
 * consumes this, applies the status and republishes the ShipmentRouteCourier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentStatusEventDTO {
    private Long assignmentId;
    private Long shipmentRouteId;
    private AssignmentStatus status;
    private ZonedDateTime occurredAt;
}

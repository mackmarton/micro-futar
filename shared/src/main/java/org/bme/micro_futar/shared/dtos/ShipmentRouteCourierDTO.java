package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRouteCourierDTO {
    private Long id;
    private Long courierId;
    private Long shipmentRouteId;
    private LocalDate dateAssignedFor;
    private Boolean pickedUpForDelivery;
    private Boolean failed;
}


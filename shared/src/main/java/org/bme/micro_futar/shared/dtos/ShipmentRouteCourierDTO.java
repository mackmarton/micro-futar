package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentRouteCourierDTO {
    private Long id;
    private Long courierId;
    private Long shipmentRouteId;
    private Date dateAssignedFor;
    private Boolean failed;
}


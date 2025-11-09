package org.bme.micro_futar.logistics.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentRouteCarrierDTO {
    private Long id;
    private Long carrierId;
    private Long shipmentRouteId;
    private Date dateAssignedFor;
    private Boolean failed;
}


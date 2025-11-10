package org.bme.micro_futar.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Entity
@NoArgsConstructor
public class ShipmentRouteCarrier {
    @Id
    private Long id;
    private Long carrierId;
    private Long shipmentRouteId;
    private Date dateAssignedFor;
    private Boolean failed = false;
}

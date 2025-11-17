package org.bme.micro_futar.tracking.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Entity
@NoArgsConstructor
public class ShipmentRouteCourier {
    @Id
    private Long id;
    private Long courierId;
    private Long shipmentRouteId;
    private Date dateAssignedFor;
    private Boolean pickedUpForDelivery = false;
    private Boolean failed = false;
}

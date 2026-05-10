package org.bme.micro_futar.courier.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
public class ShipmentRouteCourier {
    @Id
    private Long id;
    private Long courierId;
    private Long shipmentRouteId;
    private LocalDate dateAssignedFor;
    private Boolean pickedUpForDelivery = false;
    private Boolean failed = false;
}

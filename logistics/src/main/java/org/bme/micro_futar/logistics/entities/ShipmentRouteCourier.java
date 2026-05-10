package org.bme.micro_futar.logistics.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Entity
@NoArgsConstructor
public class ShipmentRouteCourier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long courierId;
    private Long shipmentRouteId;
    private ZonedDateTime dateAssignedFor;
    private Boolean pickedUpForDelivery = false;
    private Boolean failed = false;
}

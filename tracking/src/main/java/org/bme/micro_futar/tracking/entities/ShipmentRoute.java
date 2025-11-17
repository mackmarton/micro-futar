package org.bme.micro_futar.tracking.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
public class ShipmentRoute {
    @Id
    private Long id;
    private Long shipmentId;
    @Nullable
    private Long originDepoId;
    @Nullable
    private Long destinationDepoId;
    @Nullable
    private String originAddress;
    @Nullable
    private String destinationAddress;
    private Integer routePartNumber;
    @Nullable
    private Timestamp fulfillmentTime;
}

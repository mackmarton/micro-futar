package org.bme.micro_futar.logistics.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.bme.micro_futar.shared.enums.TransportType;
import org.hibernate.annotations.ColumnDefault;

import java.time.ZonedDateTime;

@Data
@Entity
@NoArgsConstructor
public class ShipmentRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private ZonedDateTime fulfillmentTime;
    @ColumnDefault("false")
    private boolean canceled;
    @Nullable
    @Enumerated(EnumType.STRING)
    private TransportType transportType;
}

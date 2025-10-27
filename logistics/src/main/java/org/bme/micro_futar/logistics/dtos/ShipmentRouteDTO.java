package org.bme.micro_futar.logistics.dtos;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ShipmentRouteDTO {
    private Long id;
    private Long originDepoId;
    private Long destinationDepoId;
    private String originAddress;
    private String destinationAddress;
    private Integer routePartNumber;
    private Timestamp fulfillmentTime;
}

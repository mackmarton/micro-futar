package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.TransportType;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRouteDTO {
    private Long id;
    private Long shipmentId;
    private Long originDepoId;
    private Long destinationDepoId;
    private String originAddress;
    private String destinationAddress;
    private Integer routePartNumber;
    private ZonedDateTime fulfillmentTime;
    private boolean canceled;
    private TransportType transportType;
}

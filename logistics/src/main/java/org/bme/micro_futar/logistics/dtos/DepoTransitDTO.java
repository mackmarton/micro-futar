package org.bme.micro_futar.logistics.dtos;

import lombok.Data;
import org.bme.micro_futar.shared.enums.TransportType;

@Data
public class DepoTransitDTO {
    private Long id;
    private Long originDepoId;
    private Long destinationDepoId;
    private Long packageSizeId;
    private TransportType transportType;
    private Double price;
}

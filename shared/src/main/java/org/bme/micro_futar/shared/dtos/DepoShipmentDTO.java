package org.bme.micro_futar.shared.dtos;

import lombok.Data;

@Data
public class DepoShipmentDTO {
    private Long id;
    private Long depoId;
    private Long shipmentId;
    private boolean deleted;
}
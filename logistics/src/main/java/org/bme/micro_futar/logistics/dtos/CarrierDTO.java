package org.bme.micro_futar.logistics.dtos;

import lombok.Data;
import org.bme.micro_futar.shared.enums.TransportType;

@Data
public class CarrierDTO {
    private Long id;
    private String name;
    private String email;
    private String telephone;
    private Long vehicleId;
    private TransportType qualifiedFor;
    private Long depoId;
}


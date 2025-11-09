package org.bme.micro_futar.shared.dtos;

import lombok.Data;
import org.bme.micro_futar.shared.enums.CourierType;
import org.bme.micro_futar.shared.enums.TransportType;

@Data
public class CourierDTO {
    private Long id;
    private String name;
    private String email;
    private String telephone;
    private Long vehicleId;
    private TransportType qualifiedFor;
    private CourierType courierType;
    private Long depoId;
}


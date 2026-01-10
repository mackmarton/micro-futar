package org.bme.micro_futar.shared.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VehicleDTO {
    private Long id;
    private String registrationNumber;
    private Double maximumPackableVolume;
}


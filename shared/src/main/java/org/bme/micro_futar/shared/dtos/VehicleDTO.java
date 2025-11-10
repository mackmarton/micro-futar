package org.bme.micro_futar.shared.dtos;

import lombok.Data;

@Data
public class VehicleDTO {
    private Long id;
    private String registrationNumber;
    private Double maximumPackableVolume;
}


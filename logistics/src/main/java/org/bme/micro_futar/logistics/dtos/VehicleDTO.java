package org.bme.micro_futar.logistics.dtos;

import lombok.Data;

@Data
public class VehicleDTO {
    private Long id;
    private String registrationNumber;
    private Long maximumPackableVolume;
}


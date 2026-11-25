package org.bme.micro_futar.courier.dtos;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MyVehicleDTO {
    String registrationNumber;
    Double maximumPackableVolume;
}

package org.bme.micro_futar.courier.dtos;

import lombok.Builder;
import lombok.Value;
import org.bme.micro_futar.shared.enums.CourierType;
import org.bme.micro_futar.shared.enums.TransportType;

/** The logged in courier's own record. */
@Value
@Builder
public class MyProfileDTO {
    Long id;
    String name;
    String email;
    String telephone;
    TransportType qualifiedFor;
    CourierType courierType;
    Long depoId;
    MyVehicleDTO vehicle;
}

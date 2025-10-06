package org.bme.micro_futar.shared.dtos;

import lombok.Data;
import lombok.NonNull;

@Data
public class LocationCityDTO {
    public Long id;
    @NonNull
    public Long countryId;
    @NonNull
    public String name;
}

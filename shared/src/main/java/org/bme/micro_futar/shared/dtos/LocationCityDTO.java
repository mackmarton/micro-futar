package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class LocationCityDTO {
    private Long id;
    @NonNull
    private Long countryId;
    @NonNull
    private String name;
}

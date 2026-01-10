package org.bme.micro_futar.shared.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationCityDTO {
    private Long id;
    @NonNull
    private Long countryId;
    @NonNull
    private String name;
}

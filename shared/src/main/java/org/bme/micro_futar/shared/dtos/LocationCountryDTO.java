package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class LocationCountryDTO {
    private Long id;
    @NonNull
    private Long regionId;
    @NonNull
    private String name;
}

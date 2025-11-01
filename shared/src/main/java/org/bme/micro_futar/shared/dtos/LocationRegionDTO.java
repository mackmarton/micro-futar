package org.bme.micro_futar.shared.dtos;

import lombok.Data;
import lombok.NonNull;

@Data
public class LocationRegionDTO {
    private Long id;
    @NonNull
    private String name;
}

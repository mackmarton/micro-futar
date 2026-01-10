package org.bme.micro_futar.shared.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationCountryDTO {
    private Long id;
    @NonNull
    private Long regionId;
    @NonNull
    private String name;
}

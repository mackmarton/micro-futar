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
    private String currencyCode;
    /** ISO 3166-1 alpha-2 code (e.g. HU); used to restrict geocoding to the country. */
    private String isoCode;
    private boolean deleted;
}

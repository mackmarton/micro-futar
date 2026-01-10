package org.bme.micro_futar.shared.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryPriceDTO {
    private Long id;
    @NonNull
    private Long originCountryId;
    @NonNull
    private Long destinationCountryId;
    @NonNull
    private Long packageSizeId;
    @NonNull
    private Double minPrice;
    @NonNull
    private Double maxPrice;
}

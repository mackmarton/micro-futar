package org.bme.micro_futar.shared.dtos;

import lombok.Data;
import lombok.NonNull;

@Data
public class CountryPriceDTO {
    public Long id;
    @NonNull
    public Long originCountryId;
    @NonNull
    public Long destinationCountryId;
    @NonNull
    public Long packageSizeId;
    @NonNull
    public Double price;
}

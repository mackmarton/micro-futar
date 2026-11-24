package org.bme.micro_futar.logistics.dtos;

import lombok.Data;

@Data
public class CountryBaseFeeDTO {
    private Long id;
    private Long countryId;
    private Long packageSizeId;
    private Double price;
}

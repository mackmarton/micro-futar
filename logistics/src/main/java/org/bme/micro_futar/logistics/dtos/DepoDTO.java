package org.bme.micro_futar.logistics.dtos;

import lombok.Data;

@Data
public class DepoDTO {
    private Long id;
    private Long locationCountryId;
    private String zip;
    private Long locationCityId;
    private String address;
    private boolean isMainDepo;
}

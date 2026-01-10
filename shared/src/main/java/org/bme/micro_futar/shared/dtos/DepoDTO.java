package org.bme.micro_futar.shared.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DepoDTO {
    private Long id;
    private Long locationCountryId;
    private String zip;
    private Long locationCityId;
    private String address;
    private Double latitude;
    private Double longitude;
    private boolean isMainDepo;
}

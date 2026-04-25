package org.bme.micro_futar.courier.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Depo {
    @Id
    private Long id;
    private String name;
    private Long locationCountryId;
    private String zip;
    private Long locationCityId;
    private String address;
    private Double latitude;
    private Double longitude;
}

package org.bme.micro_futar.tracking.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class LocationCountry {
    @Id
    private Long id;
    private Long regionId;
    private String name;

}

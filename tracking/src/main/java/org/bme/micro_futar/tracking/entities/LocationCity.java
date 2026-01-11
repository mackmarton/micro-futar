package org.bme.micro_futar.tracking.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class LocationCity {
    @Id
    private Long id;
    private Long countryId;
    private String name;
}

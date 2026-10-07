package org.bme.micro_futar.orders.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Data
@NoArgsConstructor
public class LocationCountry {
    @Id
    private Long id;
    private Long regionId;
    private String name;
    @ColumnDefault("'HUF'")
    private String currencyCode;

}

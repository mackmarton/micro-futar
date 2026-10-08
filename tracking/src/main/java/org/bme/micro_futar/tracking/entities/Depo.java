package org.bme.micro_futar.tracking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Filter;

@Data
@Entity
@NoArgsConstructor
@Filter(name = "notDeleted")
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
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}

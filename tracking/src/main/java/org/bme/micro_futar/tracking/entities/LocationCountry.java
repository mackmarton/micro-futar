package org.bme.micro_futar.tracking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Filter;

@Entity
@Data
@NoArgsConstructor
@Filter(name = "notDeleted")
public class LocationCountry {
    @Id
    private Long id;
    private Long regionId;
    private String name;
    @ColumnDefault("'HUF'")
    private String currencyCode;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}

package org.bme.micro_futar.orders.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Filter;

import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
@Filter(name = "notDeleted")
public class LocationCity {
    @Id
    private Long id;
    private Long countryId;
    private String name;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double bboxMinLatitude;
    private Double bboxMinLongitude;
    private Double bboxMaxLatitude;
    private Double bboxMaxLongitude;
    private ZonedDateTime geocodedAt;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean geoManual;
}

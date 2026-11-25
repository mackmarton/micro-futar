package org.bme.micro_futar.logistics.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZonedDateTime;

@Data
@Entity
@NoArgsConstructor
@SQLDelete(sql = "UPDATE location_city SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class LocationCity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

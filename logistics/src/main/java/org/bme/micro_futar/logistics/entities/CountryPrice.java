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

@Data
@Entity
@NoArgsConstructor
@SQLDelete(sql = "UPDATE country_price SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class CountryPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long originCountryId;
    private Long destinationCountryId;
    private Long packageSizeId;
    private Double minPrice;
    private Double maxPrice;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}

package org.bme.micro_futar.orders.entities;

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
public class CountryPrice {
    @Id
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

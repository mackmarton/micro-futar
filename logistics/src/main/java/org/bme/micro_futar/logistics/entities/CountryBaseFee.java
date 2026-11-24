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

/**
 * Base fee charged for a package size when the sender is in the given country. Private to logistics: orders sees its
 * effect through {@link CountryPrice}. (countryId, packageSizeId) is unique among non-deleted rows, which the service
 * enforces (a DB constraint would clash with soft-deleted rows).
 */
@Data
@Entity
@NoArgsConstructor
@SQLDelete(sql = "UPDATE country_base_fee SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class CountryBaseFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long countryId;
    private Long packageSizeId;
    private Double price;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}

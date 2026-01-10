package org.bme.micro_futar.orders.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class CountryPrice {
    @Id
    private Long id;
    private Long originCountryId;
    private Long destinationCountryId;
    private Long packageSizeId;
    private Double price;
}

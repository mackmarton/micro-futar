package org.bme.micro_futar.courier.entities;

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
public class Vehicle {
    @Id
    private Long id;
    private String registrationNumber;
    private Double maximumPackableVolume;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}

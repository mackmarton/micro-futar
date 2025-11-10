package org.bme.micro_futar.courier.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Vehicle {
    @Id
    private Long id;
    private String registrationNumber;
    private Double maximumPackableVolume;
}

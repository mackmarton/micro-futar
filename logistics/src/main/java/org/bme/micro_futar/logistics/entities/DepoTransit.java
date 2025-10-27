package org.bme.micro_futar.logistics.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.TransportType;

@Data
@Entity
@NoArgsConstructor
public class DepoTransit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long originDepoId;
    private Long destinationDepoId;
    private Long packageSizeId;
    private TransportType transportType;
    private Double price;
    private Double durationInHours;
}

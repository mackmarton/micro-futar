package org.bme.micro_futar.logistics.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.TransportType;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Data
@Entity
@NoArgsConstructor
@SQLDelete(sql = "UPDATE depo_transit SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class DepoTransit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long originDepoId;
    private Long destinationDepoId;
    private Long packageSizeId;
    private TransportType transportType;
    private Double price;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}

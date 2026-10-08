package org.bme.micro_futar.logistics.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.CourierType;
import org.bme.micro_futar.shared.enums.TransportType;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Data
@Entity
@NoArgsConstructor
@SQLDelete(sql = "UPDATE courier SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String telephone;
    private Long vehicleId;
    private TransportType qualifiedFor;
    private CourierType courierType;
    @Nullable
    private Long depoId;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}

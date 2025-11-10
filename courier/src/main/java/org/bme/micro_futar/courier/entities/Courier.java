package org.bme.micro_futar.courier.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.CourierType;
import org.bme.micro_futar.shared.enums.TransportType;

@Data
@Entity
@NoArgsConstructor
public class Courier {
    @Id
    private Long id;
    private String name;
    private String email;
    private String telephone;
    private Long vehicleId;
    private TransportType qualifiedFor;
    private CourierType courierType;
    @Nullable
    private Long depoId;
}

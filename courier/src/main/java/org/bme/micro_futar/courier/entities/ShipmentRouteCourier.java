package org.bme.micro_futar.courier.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
/** Read-only copy of the assignment owned by logistics; courier's own facts are in {@link AssignmentProgress}. */
public class ShipmentRouteCourier {
    @Id
    private Long id;
    private Long courierId;
    private Long shipmentRouteId;
    private LocalDate dateAssignedFor;
    @ColumnDefault("false")
    private boolean canceled;
}

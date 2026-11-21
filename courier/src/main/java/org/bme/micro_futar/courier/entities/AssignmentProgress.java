package org.bme.micro_futar.courier.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * What the courier did with an assignment. Owned by courier and kept apart from the read-only
 * {@link ShipmentRouteCourier} copy so that incoming messages from logistics never overwrite it.
 */
@Data
@Entity
@NoArgsConstructor
public class AssignmentProgress {
    @Id
    private Long assignmentId;
    private ZonedDateTime pickedUpAt;
    private ZonedDateTime failedAt;
    private ZonedDateTime fulfilledAt;
}

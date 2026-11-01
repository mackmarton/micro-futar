package org.bme.micro_futar.payments.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/** Stripe delivers webhooks at least once; the event id is stored so a redelivery is ignored. */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedStripeEvent {
    @Id
    private String id;
    private String type;
    private ZonedDateTime processedAt;
}

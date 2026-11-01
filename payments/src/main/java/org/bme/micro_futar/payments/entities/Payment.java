package org.bme.micro_futar.payments.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private Long shipmentId;
    private String parcelNumber;
    private String senderEmail;
    /** SHA-256 hex of the token the sender received from orders; proves ownership without an account. */
    @Column(nullable = false)
    private String paymentTokenHash;
    @Column(nullable = false)
    private BigDecimal maxAmount;
    private BigDecimal capturedAmount;
    @Column(nullable = false)
    private String currencyCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    private String checkoutSessionId;
    @Column(length = 1024)
    private String checkoutUrl;
    private ZonedDateTime checkoutExpiresAt;
    private String paymentIntentId;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}

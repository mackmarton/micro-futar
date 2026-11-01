package org.bme.micro_futar.shared.enums;

public enum PaymentStatus {
    /** Checkout session created, waiting for the sender to complete it. */
    PENDING,
    /** Card authorized for the maximum price, not yet captured. */
    AUTHORIZED,
    /** Final price captured. */
    PAID,
    /** Checkout session expired without payment. */
    EXPIRED,
    /** Authorization or capture failed. */
    FAILED,
    /** Authorization released before capture (e.g. shipment cancelled). */
    CANCELED,
    /** Captured amount returned to the sender. */
    REFUNDED
}

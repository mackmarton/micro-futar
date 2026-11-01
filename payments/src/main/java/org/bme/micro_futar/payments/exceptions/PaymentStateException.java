package org.bme.micro_futar.payments.exceptions;

import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentStateException extends RuntimeException {
    public PaymentStateException(Long shipmentId, PaymentStatus status) {
        super("Payment for shipment " + shipmentId + " is in status " + status);
    }
}

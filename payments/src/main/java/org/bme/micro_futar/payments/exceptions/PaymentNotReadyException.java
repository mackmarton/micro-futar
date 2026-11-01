package org.bme.micro_futar.payments.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** The payment request from orders has not arrived via Kafka yet; the client should retry shortly. */
@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentNotReadyException extends RuntimeException {
    public PaymentNotReadyException(Long shipmentId) {
        super("Payment for shipment " + shipmentId + " is not ready yet");
    }
}

package org.bme.micro_futar.orders.exceptions;

public class KafkaException extends RuntimeException {
    public KafkaException(String message) {
        super(message);
    }

    public KafkaException(String message, Exception e) {
        super(message, e);
    }
}

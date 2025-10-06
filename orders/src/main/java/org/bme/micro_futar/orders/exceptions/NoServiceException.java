package org.bme.micro_futar.orders.exceptions;

public class NoServiceException extends RuntimeException {
    public NoServiceException(String message) {
        super(message);
    }
}

package org.bme.micro_futar.logistics.exceptions;

public class NoRouteFoundException extends RuntimeException {
    public NoRouteFoundException(String message) {
        super(message);
    }
}

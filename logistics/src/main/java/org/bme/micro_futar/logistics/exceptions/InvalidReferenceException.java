package org.bme.micro_futar.logistics.exceptions;

import lombok.Getter;

/** Thrown when a write points to a logistics-owned entity that does not exist or is soft-deleted. */
@Getter
public class InvalidReferenceException extends RuntimeException {

    private final String field;

    public InvalidReferenceException(String field, String message) {
        super(message);
        this.field = field;
    }
}

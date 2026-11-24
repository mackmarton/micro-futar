package org.bme.micro_futar.logistics.exceptions;

import lombok.Getter;

/** Thrown when a write would break a uniqueness rule that the service (not the database) enforces. */
@Getter
public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String message) {
        super(message);
    }
}

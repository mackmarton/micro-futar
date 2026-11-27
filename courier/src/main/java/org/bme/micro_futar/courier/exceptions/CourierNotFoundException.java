package org.bme.micro_futar.courier.exceptions;

import java.util.NoSuchElementException;

/** The caller is logged in, but there is no courier with their email. */
public class CourierNotFoundException extends NoSuchElementException {

    public CourierNotFoundException() {
        super("There is no courier with the caller's email");
    }
}

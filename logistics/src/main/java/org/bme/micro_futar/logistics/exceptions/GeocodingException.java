package org.bme.micro_futar.logistics.exceptions;

/** The geocoder could not be reached or answered with an error (timeout, quota, 5xx...). */
public class GeocodingException extends RuntimeException {
    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeocodingException(String message) {
        super(message);
    }
}

package org.bme.micro_futar.logistics.exceptions;

/** An explicitly requested geocoding found nothing (or the country has no ISO code). */
public class GeocodingNotFoundException extends RuntimeException {
    public GeocodingNotFoundException(String message) {
        super(message);
    }
}

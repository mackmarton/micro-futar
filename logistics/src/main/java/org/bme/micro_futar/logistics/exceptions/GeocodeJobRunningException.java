package org.bme.micro_futar.logistics.exceptions;

public class GeocodeJobRunningException extends RuntimeException {
    public GeocodeJobRunningException() {
        super("A bulk geocoding run is already in progress");
    }
}

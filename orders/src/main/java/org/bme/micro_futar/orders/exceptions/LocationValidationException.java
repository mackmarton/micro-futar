package org.bme.micro_futar.orders.exceptions;

import lombok.Getter;

/** The coordinates of a shipment party do not fit the city they were given with; maps to HTTP 400. */
@Getter
public class LocationValidationException extends RuntimeException {

    public enum Party { SENDER, RECIPIENT }

    public enum Code {
        INVALID_COORDINATES,
        CITY_NOT_FOUND,
        CITY_COUNTRY_MISMATCH,
        LOCATION_TOO_FAR_FROM_CITY
    }

    private final Code code;
    private final Party party;

    public LocationValidationException(Code code, Party party, String message) {
        super(message);
        this.code = code;
        this.party = party;
    }
}

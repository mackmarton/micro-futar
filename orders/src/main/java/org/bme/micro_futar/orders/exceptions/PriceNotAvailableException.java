package org.bme.micro_futar.orders.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PriceNotAvailableException extends RuntimeException {
    public PriceNotAvailableException(Long originCountryId, Long destinationCountryId, Long packageSizeId) {
        super("No price for route " + originCountryId + " -> " + destinationCountryId
                + " with package size " + packageSizeId);
    }
}

package org.bme.micro_futar.orders.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Machine-readable: {@code code} (e.g. LOCATION_TOO_FAR_FROM_CITY) and {@code party} (SENDER / RECIPIENT). */
    @ExceptionHandler(LocationValidationException.class)
    public ProblemDetail handleLocationValidation(LocationValidationException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setProperty("code", e.getCode().name());
        problem.setProperty("party", e.getParty().name());
        return problem;
    }
}

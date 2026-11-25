package org.bme.micro_futar.orders.exceptions;

import org.bme.micro_futar.shared.exceptions.EmailNotVerifiedException;
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

    /** The caller is logged in, but their email is not verified: {@code code} is EMAIL_NOT_VERIFIED. */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ProblemDetail handleEmailNotVerified(EmailNotVerifiedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problem.setProperty("code", EmailNotVerifiedException.CODE);
        return problem;
    }
}

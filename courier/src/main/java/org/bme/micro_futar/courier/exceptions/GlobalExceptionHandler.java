package org.bme.micro_futar.courier.exceptions;

import org.bme.micro_futar.shared.exceptions.EmailNotVerifiedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** The caller is logged in, but their email is not verified: {@code code} is EMAIL_NOT_VERIFIED. */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ProblemDetail handleEmailNotVerified(EmailNotVerifiedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problem.setProperty("code", EmailNotVerifiedException.CODE);
        return problem;
    }

    @ExceptionHandler(CourierNotFoundException.class)
    public ProblemDetail handleCourierNotFound(CourierNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}

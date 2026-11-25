package org.bme.micro_futar.orders.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTests {

    @Test
    void locationValidationBecomesBadRequestWithMachineReadableCodeAndParty() {
        ProblemDetail problem = new GlobalExceptionHandler().handleLocationValidation(
                new LocationValidationException(LocationValidationException.Code.LOCATION_TOO_FAR_FROM_CITY,
                        LocationValidationException.Party.RECIPIENT, "too far"));

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getProperties())
                .containsEntry("code", "LOCATION_TOO_FAR_FROM_CITY")
                .containsEntry("party", "RECIPIENT");
    }
}

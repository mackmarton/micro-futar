package org.bme.micro_futar.payments.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    private Long shipmentId;
    private String paymentToken;
    /** UI language ({@code hu} or {@code en}): Stripe's page language and where it redirects back to. */
    private String locale;
}

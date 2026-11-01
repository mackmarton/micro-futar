package org.bme.micro_futar.payments.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.PaymentStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponseDTO {
    private Long shipmentId;
    private PaymentStatus status;
    private BigDecimal maxAmount;
    private BigDecimal capturedAmount;
    private String currencyCode;
}

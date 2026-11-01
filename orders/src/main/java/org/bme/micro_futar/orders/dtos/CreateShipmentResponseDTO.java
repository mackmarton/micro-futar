package org.bme.micro_futar.orders.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;

/**
 * Response of shipment creation. The payment token is returned only here, never stored in plain form and never
 * put on Kafka: it is what lets a sender without an account start the checkout for this shipment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentResponseDTO {
    private ShipmentDTO shipment;
    private String paymentToken;
}

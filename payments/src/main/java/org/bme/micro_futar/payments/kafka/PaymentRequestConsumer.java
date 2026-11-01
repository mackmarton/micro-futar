package org.bme.micro_futar.payments.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.payments.services.PaymentService;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.payment-request-topic}", groupId = "payments-payment-request")
    public void consumePaymentRequest(String message) {
        try {
            PaymentRequestedEventDTO request = objectMapper.readValue(message, PaymentRequestedEventDTO.class);
            // The message holds the sender's email and the token hash, so only the id is logged.
            log.info("Received payment request for shipment {}", request.getShipmentId());
            paymentService.registerPaymentRequest(request);
        } catch (Exception e) {
            log.error("Error processing payment request message", e);
            throw new KafkaException("Failed to process payment request message", e);
        }
    }
}

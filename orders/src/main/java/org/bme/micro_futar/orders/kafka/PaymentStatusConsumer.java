package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentStatusConsumer {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.payment-status-topic}", groupId = "orders-payment-status")
    public void consumePaymentStatus(String message) {
        log.info("Received payment status message: {}", message);
        try {
            PaymentStatusEventDTO event = objectMapper.readValue(message, PaymentStatusEventDTO.class);
            shipmentService.applyPaymentStatus(event);
        } catch (Exception e) {
            log.error("Error processing payment status message: {}", message, e);
            throw new KafkaException("Failed to process payment status message", e);
        }
    }
}

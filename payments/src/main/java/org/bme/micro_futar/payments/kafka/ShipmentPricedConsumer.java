package org.bme.micro_futar.payments.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.payments.services.PaymentService;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentPricedConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.shipment-priced-topic}", groupId = "payments-shipment-priced")
    public void consumeShipmentPriced(String message) {
        log.info("Received shipment priced message: {}", message);
        try {
            ShipmentPricedEventDTO priced = objectMapper.readValue(message, ShipmentPricedEventDTO.class);
            paymentService.capture(priced);
        } catch (Exception e) {
            log.error("Error processing shipment priced message: {}", message, e);
            throw new KafkaException("Failed to process shipment priced message", e);
        }
    }
}

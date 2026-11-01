package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.payment-request-topic}")
    private String paymentRequestTopic;

    /**
     * Runs after the shipment is committed, so payments never gets a request for a shipment that was rolled back.
     * TODO: replace with a transactional outbox; a crash between commit and send currently loses the request.
     */
    @TransactionalEventListener
    public void sendPaymentRequestToTopic(PaymentRequestedEventDTO request) {
        // The message holds the sender's email and the token hash, so only the id is logged.
        log.info("Sending payment request for shipment {} to Kafka", request.getShipmentId());
        try {
            String requestJSON = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(paymentRequestTopic, request.getShipmentId().toString(), requestJSON)
                    .whenComplete((_, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send payment request for shipment {}", request.getShipmentId(), ex);
                        } else {
                            log.info("Successfully sent payment request for shipment {}", request.getShipmentId());
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending payment request for shipment {}", request.getShipmentId(), e);
        }
    }
}

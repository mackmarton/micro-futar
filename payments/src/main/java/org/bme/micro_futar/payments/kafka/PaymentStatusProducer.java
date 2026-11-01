package org.bme.micro_futar.payments.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.payment-status-topic}")
    private String paymentStatusTopic;

    /**
     * Runs after the payment change is committed, so consumers never see a status that was rolled back.
     * TODO: replace with a transactional outbox; a crash between commit and send currently loses the event.
     */
    @TransactionalEventListener
    public void sendPaymentStatusToTopic(PaymentStatusEventDTO event) {
        log.info("Sending payment status event to Kafka: {}", event.getDedupKey());
        try {
            String eventJSON = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(paymentStatusTopic, event.getShipmentId().toString(), eventJSON)
                    .whenComplete((_, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send payment status event to Kafka: {}", event.getDedupKey(), ex);
                        } else {
                            log.info("Successfully sent payment status event: {}", event.getDedupKey());
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending payment status event to Kafka: {}", event.getDedupKey(), e);
        }
    }
}

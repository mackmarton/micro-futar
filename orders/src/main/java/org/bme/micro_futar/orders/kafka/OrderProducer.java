package org.bme.micro_futar.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.OrderDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;

    @Value("${kafka.topics.order-topic}")
    private String orderTopic;

    @Transactional
    public void sendOrder(OrderDTO orderDTO) {
        log.info("Sending order message to Kafka: {}", orderDTO);
        try {
            String key = orderDTO.getId() != null ? orderDTO.getId().toString() : "";
            CompletableFuture<SendResult<String, OrderDTO>> future = kafkaTemplate.send(orderTopic, key, orderDTO);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send order message to Kafka: {}", orderDTO, ex);
                } else {
                    log.info("Successfully sent order message with ID: {} to partition: {}",
                            orderDTO.getId(), result.getRecordMetadata().partition());
                }
            });
        } catch (Exception e) {
            log.error("Error sending order message to Kafka: {}", orderDTO, e);
            throw e;
        }
    }
}

package org.bme.micro_futar.logistics.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.services.OrderService;
import org.bme.micro_futar.shared.dtos.OrderDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${kafka.topics.order-topic}", groupId = "logistics-group")
    public void consumeOrder(OrderDTO orderDTO) {
        log.info("Received order message: {}", orderDTO);
        try {
            orderService.processOrder(orderDTO);
            log.info("Successfully processed order with ID: {}", orderDTO.getId());
        } catch (Exception e) {
            log.error("Error processing order with ID: {}", orderDTO.getId(), e);
        }
    }
}

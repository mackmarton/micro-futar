package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.services.CurrencyService;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyConsumer {

    private final CurrencyService currencyService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.currency-topic}", groupId = "orders-currency")
    public void consumeCurrency(String message) {
        log.info("Received currency message: {}", message);

        try {
            CurrencyDTO currencyDTO = objectMapper.readValue(message, CurrencyDTO.class);
            CurrencyDTO currency = currencyService.saveCurrency(currencyDTO);
            log.info("Successfully saved currency with ID: {}", currency.getId());
        } catch (Exception e) {
            log.error("Error processing currency message: {}", message, e);
            throw new KafkaException("Failed to process currency message", e);
        }
    }
}

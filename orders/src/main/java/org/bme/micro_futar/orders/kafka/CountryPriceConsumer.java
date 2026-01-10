package org.bme.micro_futar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.services.CountryPriceService;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CountryPriceConsumer {

    private final CountryPriceService countryPriceService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.country-price-topic}", groupId = "orders-service")
    public void consumeCountryPrice(String message) {
        log.info("Received country price message: {}", message);

        try {
            CountryPriceDTO countryPriceDTO = objectMapper.readValue(message, CountryPriceDTO.class);
            CountryPriceDTO countryPrice = countryPriceService.saveCountryPrice(countryPriceDTO);
            log.info("Successfully saved country price with ID: {}", countryPrice.getId());
        } catch (Exception e) {
            log.error("Error processing country price message: {}", message, e);
            throw new KafkaException("Failed to process country price message", e);
        }
    }
}

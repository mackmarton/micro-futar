package org.bme.micro_futar.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.CountryPrice;
import org.bme.micro_futar.orders.services.CountryPriceService;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CountryPriceConsumer {

    private final CountryPriceService countryPriceService;

    @KafkaListener(topics = "${kafka.topics.country-price-topic}", groupId = "orders-service")
    public void consumeCountryPrice(CountryPriceDTO countryPriceDTO) {
        log.info("Received country price message: {}", countryPriceDTO);

        try {
            CountryPrice countryPrice = countryPriceService.saveCountryPrice(countryPriceDTO);
            log.info("Successfully saved country price with ID: {}", countryPrice.getId());
        } catch (Exception e) {
            log.error("Error processing country price message: {}", countryPriceDTO, e);
            throw e;
        }
    }
}

package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.bme.micro_futar.logistics.repositories.CountryBaseFeeRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentPricingService {

    private final CountryBaseFeeRepository countryBaseFeeRepository;

    public BigDecimal finalPrice(ShipmentDTO shipment, double routePrice) {
        BigDecimal max = BigDecimal.valueOf(shipment.getMaxPrice());
        BigDecimal baseFee = countryBaseFeeRepository.findByCountryIdAndPackageSizeId(
                        shipment.getSenderLocationCountryId(), shipment.getPackageSizeId())
                .map(CountryBaseFee::getPrice)
                .map(BigDecimal::valueOf)
                .orElseGet(() -> {
                    log.warn("No base fee for country {} and package size {} (shipment {}), counting it as 0",
                            shipment.getSenderLocationCountryId(), shipment.getPackageSizeId(), shipment.getId());
                    return BigDecimal.ZERO;
                });
        BigDecimal price = baseFee.add(BigDecimal.valueOf(routePrice));

        if (price.compareTo(max) > 0) {
            log.warn("Price {} (base fee {} + route {}) for shipment {} exceeds the authorized {}, capping it",
                    price, baseFee, routePrice, shipment.getId(), max);
            return max;
        }
        return price;
    }
}

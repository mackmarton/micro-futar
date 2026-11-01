package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentPricingService {

    private final CountryPriceRepository countryPriceRepository;

    /**
     * The final price is the cheapest depo route between the depos closest to the sender and the recipient, kept
     * within the price range the sender saw: never above the {@code maxPrice} authorized on their card, and never
     * below the route's minimum (the closest depos can be the same one, which would make the route free).
     */
    public BigDecimal finalPrice(ShipmentDTO shipment, double routePrice) {
        BigDecimal max = BigDecimal.valueOf(shipment.getMaxPrice());
        BigDecimal price = BigDecimal.valueOf(routePrice);
        BigDecimal min = countryPriceRepository.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(
                        shipment.getSenderLocationCountryId(), shipment.getRecipientLocationCountryId(),
                        shipment.getPackageSizeId())
                .map(CountryPrice::getMinPrice)
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO)
                // The price table may have changed since the order; the authorized maximum still wins.
                .min(max);

        if (price.compareTo(max) > 0) {
            log.warn("Route price {} for shipment {} exceeds the authorized {}, capping it",
                    price, shipment.getId(), max);
            return max;
        }
        if (price.compareTo(min) < 0) {
            log.info("Route price {} for shipment {} is below the minimum {}, raising it",
                    price, shipment.getId(), min);
            return min;
        }
        return price;
    }
}

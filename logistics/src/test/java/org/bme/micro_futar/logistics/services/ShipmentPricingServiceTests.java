package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentPricingServiceTests {

    @Mock
    private CountryPriceRepository countryPriceRepository;

    @InjectMocks
    private ShipmentPricingService shipmentPricingService;

    private ShipmentDTO shipment;

    @BeforeEach
    void setUp() {
        shipment = new ShipmentDTO();
        shipment.setId(1L);
        shipment.setSenderLocationCountryId(1L);
        shipment.setRecipientLocationCountryId(2L);
        shipment.setPackageSizeId(3L);
        shipment.setMaxPrice(3000.0);
    }

    private void givenMinPrice(double minPrice) {
        CountryPrice countryPrice = new CountryPrice();
        countryPrice.setMinPrice(minPrice);
        countryPrice.setMaxPrice(3000.0);
        when(countryPriceRepository.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(1L, 2L, 3L))
                .thenReturn(Optional.of(countryPrice));
    }

    @Test
    void routePriceWithinRangeIsUsed() {
        givenMinPrice(1000.0);

        assertThat(shipmentPricingService.finalPrice(shipment, 2490.0)).isEqualByComparingTo("2490");
    }

    @Test
    void routePriceIsCappedAtAuthorizedMaximum() {
        givenMinPrice(1000.0);

        assertThat(shipmentPricingService.finalPrice(shipment, 3500.0)).isEqualByComparingTo("3000");
    }

    @Test
    void sameDepoRouteIsRaisedToMinimum() {
        givenMinPrice(1000.0);

        assertThat(shipmentPricingService.finalPrice(shipment, 0.0)).isEqualByComparingTo("1000");
    }

    @Test
    void minimumAboveAuthorizedMaximumFallsBackToMaximum() {
        givenMinPrice(4000.0);

        assertThat(shipmentPricingService.finalPrice(shipment, 0.0)).isEqualByComparingTo("3000");
    }
}

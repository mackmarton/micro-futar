package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.bme.micro_futar.logistics.repositories.CountryBaseFeeRepository;
import org.bme.micro_futar.logistics.support.TestFixtures;
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
    private CountryBaseFeeRepository countryBaseFeeRepository;

    @InjectMocks
    private ShipmentPricingService shipmentPricingService;

    private ShipmentDTO shipment;

    @BeforeEach
    void setUp() {
        shipment = TestFixtures.shipment(1L).maxPrice(3000.0).build();
    }

    private void givenBaseFee(double price) {
        CountryBaseFee fee = new CountryBaseFee();
        fee.setPrice(price);
        when(countryBaseFeeRepository.findByCountryIdAndPackageSizeId(
                TestFixtures.HUNGARY_ID, TestFixtures.SMALL_PACKAGE_ID)).thenReturn(Optional.of(fee));
    }

    @Test
    void sameDepoCostsOnlyTheBaseFee() {
        givenBaseFee(1000.0);

        assertThat(shipmentPricingService.finalPrice(shipment, 0.0)).isEqualByComparingTo("1000");
    }

    @Test
    void differentDeposCostBaseFeePlusRoute() {
        givenBaseFee(1000.0);

        assertThat(shipmentPricingService.finalPrice(shipment, 1490.0)).isEqualByComparingTo("2490");
    }

    @Test
    void priceAboveAuthorizedMaximumIsCapped() {
        givenBaseFee(1000.0);

        assertThat(shipmentPricingService.finalPrice(shipment, 2500.0)).isEqualByComparingTo("3000");
    }

    @Test
    void missingBaseFeeCountsAsZero() {
        when(countryBaseFeeRepository.findByCountryIdAndPackageSizeId(
                TestFixtures.HUNGARY_ID, TestFixtures.SMALL_PACKAGE_ID)).thenReturn(Optional.empty());

        assertThat(shipmentPricingService.finalPrice(shipment, 1490.0)).isEqualByComparingTo("1490");
    }
}

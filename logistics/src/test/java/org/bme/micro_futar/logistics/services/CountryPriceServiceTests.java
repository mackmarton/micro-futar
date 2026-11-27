package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.CountryPriceMapper;
import org.bme.micro_futar.logistics.repositories.CountryBaseFeeRepository;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryPriceServiceTests {

    private static final long HU = 1L;
    private static final long AT = 2L;
    private static final long SIZE = 7L;

    @Mock private CountryPriceRepository countryPriceRepository;
    @Mock private KafkaProducerService kafkaProducerService;
    @Mock private DepoService depoService;
    @Mock private DepoRouteFinderService depoRouteFinderService;
    @Mock private PackageSizeService packageSizeService;
    @Mock private CountryBaseFeeRepository countryBaseFeeRepository;

    private CountryPriceService countryPriceService;

    private DepoDTO huDepo;
    private DepoDTO atDepo;

    private static DepoDTO depo(long id, long countryId) {
        DepoDTO depo = TestFixtures.budapestDepo(countryId, null);
        depo.setId(id);
        return depo;
    }

    private static CountryBaseFee fee(long countryId, double price) {
        CountryBaseFee fee = TestFixtures.entity(TestFixtures.baseFee(countryId, SIZE));
        fee.setPrice(price);
        return fee;
    }

    @BeforeEach
    void setUp() {
        countryPriceService = new CountryPriceService(countryPriceRepository, Mappers.getMapper(CountryPriceMapper.class),
                kafkaProducerService, depoService, depoRouteFinderService, packageSizeService, countryBaseFeeRepository);
        huDepo = depo(10L, HU);
        atDepo = depo(20L, AT);
        PackageSizeDTO size = TestFixtures.smallPackage().id(SIZE).build();
        lenient().when(packageSizeService.getAllPackageSizes()).thenReturn(List.of(size));
        lenient().when(countryPriceRepository.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(
                any(), any(), any())).thenReturn(Optional.empty());
        lenient().when(countryPriceRepository.save(any(CountryPrice.class))).thenAnswer(i -> i.getArgument(0));
    }

    private void givenDepos(DepoDTO... depos) {
        when(depoService.getAllDepos()).thenReturn(List.of(depos));
        lenient().when(depoService.getAllDeposByCountryId(HU)).thenReturn(
                List.of(depos).stream().filter(d -> d.getLocationCountryId() == HU).toList());
        lenient().when(depoService.getAllDeposByCountryId(AT)).thenReturn(
                List.of(depos).stream().filter(d -> d.getLocationCountryId() == AT).toList());
    }

    private List<CountryPrice> savedPrices() {
        ArgumentCaptor<CountryPrice> captor = ArgumentCaptor.forClass(CountryPrice.class);
        verify(countryPriceRepository, org.mockito.Mockito.atLeast(0)).save(captor.capture());
        return captor.getAllValues();
    }

    private CountryPrice priceFor(List<CountryPrice> prices, long origin, long destination) {
        return prices.stream()
                .filter(p -> p.getOriginCountryId() == origin && p.getDestinationCountryId() == destination)
                .findFirst().orElseThrow();
    }

    @Test
    void singleDepoCountryGetsDomesticPriceEqualToBaseFee() {
        givenDepos(huDepo);
        when(countryBaseFeeRepository.findAll()).thenReturn(List.of(fee(HU, 800.0)));

        countryPriceService.recalculate();

        CountryPrice domestic = priceFor(savedPrices(), HU, HU);
        assertThat(domestic.getMinPrice()).isEqualTo(800.0);
        assertThat(domestic.getMaxPrice()).isEqualTo(800.0);
    }

    @Test
    void pairsOfTheSameDepoCountAndBaseFeeIsAddedToRoutes() {
        DepoDTO secondHuDepo = depo(11L, HU);
        givenDepos(huDepo, secondHuDepo, atDepo);
        when(countryBaseFeeRepository.findAll()).thenReturn(List.of(fee(HU, 500.0), fee(AT, 900.0)));
        when(depoRouteFinderService.findCheapestRoutePrice(10L, 11L, SIZE)).thenReturn(300.0);
        when(depoRouteFinderService.findCheapestRoutePrice(11L, 10L, SIZE)).thenReturn(300.0);
        when(depoRouteFinderService.findCheapestRoutePrice(10L, 20L, SIZE)).thenReturn(1000.0);
        when(depoRouteFinderService.findCheapestRoutePrice(11L, 20L, SIZE)).thenReturn(1200.0);

        countryPriceService.recalculate();

        List<CountryPrice> prices = savedPrices();
        CountryPrice domestic = priceFor(prices, HU, HU);
        // same-depo pairs cost 0 as a route, so the cheapest domestic pair is the base fee alone
        assertThat(domestic.getMinPrice()).isEqualTo(500.0);
        assertThat(domestic.getMaxPrice()).isEqualTo(800.0);
        CountryPrice huToAt = priceFor(prices, HU, AT);
        assertThat(huToAt.getMinPrice()).isEqualTo(1500.0);
        assertThat(huToAt.getMaxPrice()).isEqualTo(1700.0);
    }

    @Test
    void missingBaseFeeCountsAsZeroAndKeepsCountryPrice() {
        givenDepos(huDepo, atDepo);
        when(countryBaseFeeRepository.findAll()).thenReturn(List.of());
        when(depoRouteFinderService.findCheapestRoutePrice(10L, 20L, SIZE)).thenReturn(1000.0);

        countryPriceService.recalculate();

        CountryPrice huToAt = priceFor(savedPrices(), HU, AT);
        assertThat(huToAt.getMinPrice()).isEqualTo(1000.0);
        assertThat(huToAt.getMaxPrice()).isEqualTo(1000.0);
        verify(countryPriceRepository, never()).delete(any());
    }
}

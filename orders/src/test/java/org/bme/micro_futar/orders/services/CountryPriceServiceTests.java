package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.CountryPrice;
import org.bme.micro_futar.orders.mappers.CountryPriceMapper;
import org.bme.micro_futar.orders.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryPriceServiceTests {

    @Mock
    private CountryPriceRepository countryPriceRepository;

    @Mock
    private CountryPriceMapper countryPriceMapper;

    @InjectMocks
    private CountryPriceService countryPriceService;

    private CountryPrice countryPrice1;
    private CountryPrice countryPrice2;
    private CountryPriceDTO countryPriceDTO1;
    private CountryPriceDTO countryPriceDTO2;

    @BeforeEach
    void setUp() {
        countryPrice1 = new CountryPrice();
        countryPrice1.setId(1L);
        countryPrice1.setOriginCountryId(1L);
        countryPrice1.setDestinationCountryId(2L);
        countryPrice1.setPackageSizeId(1L);

        countryPrice2 = new CountryPrice();
        countryPrice2.setId(2L);
        countryPrice2.setOriginCountryId(1L);
        countryPrice2.setDestinationCountryId(3L);
        countryPrice2.setPackageSizeId(2L);

        countryPriceDTO1 = CountryPriceDTO.builder()
                .id(1L)
                .originCountryId(1L)
                .destinationCountryId(2L)
                .packageSizeId(1L)
                .minPrice(10.0)
                .maxPrice(15.0)
                .build();

        countryPriceDTO2 = CountryPriceDTO.builder()
                .id(2L)
                .originCountryId(1L)
                .destinationCountryId(3L)
                .packageSizeId(2L)
                .minPrice(10.0)
                .maxPrice(15.0)
                .build();
    }

    @Test
    void testGetAllCountryPrices() {
        List<CountryPrice> prices = Arrays.asList(countryPrice1, countryPrice2);
        when(countryPriceRepository.findAll()).thenReturn(prices);
        when(countryPriceMapper.toDTO(countryPrice1)).thenReturn(countryPriceDTO1);
        when(countryPriceMapper.toDTO(countryPrice2)).thenReturn(countryPriceDTO2);

        List<CountryPriceDTO> result = countryPriceService.getAllCountryPrices();

        assertThat(result).hasSize(2);
        verify(countryPriceRepository).findAll();
        verify(countryPriceMapper, times(2)).toDTO(any(CountryPrice.class));
    }

    @Test
    void testGetAllCountryPrices_EmptyList() {
        when(countryPriceRepository.findAll()).thenReturn(List.of());

        List<CountryPriceDTO> result = countryPriceService.getAllCountryPrices();

        assertThat(result).isEmpty();
        verify(countryPriceRepository).findAll();
    }

    @Test
    void testGetCountryPriceById_Found() {
        when(countryPriceRepository.findById(1L)).thenReturn(Optional.of(countryPrice1));
        when(countryPriceMapper.toDTO(countryPrice1)).thenReturn(countryPriceDTO1);

        Optional<CountryPriceDTO> result = countryPriceService.getCountryPriceById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(countryPriceRepository).findById(1L);
        verify(countryPriceMapper).toDTO(countryPrice1);
    }

    @Test
    void testGetCountryPriceById_NotFound() {
        when(countryPriceRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CountryPriceDTO> result = countryPriceService.getCountryPriceById(999L);

        assertThat(result).isEmpty();
        verify(countryPriceRepository).findById(999L);
        verify(countryPriceMapper, never()).toDTO(any());
    }

    @Test
    void testFindPriceByCountriesAndSize_Found() {
        long originCountryId = 1L;
        long destinationCountryId = 2L;
        long packageSizeId = 1L;

        when(countryPriceRepository.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(
                originCountryId, destinationCountryId, packageSizeId))
                .thenReturn(Optional.of(countryPrice1));
        when(countryPriceMapper.toDTO(countryPrice1)).thenReturn(countryPriceDTO1);

        Optional<CountryPriceDTO> result = countryPriceService.findPriceByCountriesAndSize(
                originCountryId, destinationCountryId, packageSizeId);

        assertThat(result).isPresent();
        assertThat(result.get().getOriginCountryId()).isEqualTo(originCountryId);
        assertThat(result.get().getDestinationCountryId()).isEqualTo(destinationCountryId);
        assertThat(result.get().getPackageSizeId()).isEqualTo(packageSizeId);
        verify(countryPriceRepository).findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(
                originCountryId, destinationCountryId, packageSizeId);
    }

    @Test
    void testFindPriceByCountriesAndSize_NotFound() {
        long originCountryId = 1L;
        long destinationCountryId = 999L;
        long packageSizeId = 1L;

        when(countryPriceRepository.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(
                originCountryId, destinationCountryId, packageSizeId))
                .thenReturn(Optional.empty());

        Optional<CountryPriceDTO> result = countryPriceService.findPriceByCountriesAndSize(
                originCountryId, destinationCountryId, packageSizeId);

        assertThat(result).isEmpty();
        verify(countryPriceRepository).findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(
                originCountryId, destinationCountryId, packageSizeId);
        verify(countryPriceMapper, never()).toDTO(any());
    }

    @Test
    void testSaveCountryPrice() {
        when(countryPriceMapper.toEntity(countryPriceDTO1)).thenReturn(countryPrice1);
        when(countryPriceRepository.save(countryPrice1)).thenReturn(countryPrice1);
        when(countryPriceMapper.toDTO(countryPrice1)).thenReturn(countryPriceDTO1);

        CountryPriceDTO result = countryPriceService.saveCountryPrice(countryPriceDTO1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(countryPriceMapper).toEntity(countryPriceDTO1);
        verify(countryPriceRepository).save(countryPrice1);
        verify(countryPriceMapper).toDTO(countryPrice1);
    }
}

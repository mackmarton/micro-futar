package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.LocationCountry;
import org.bme.micro_futar.orders.mappers.LocationCountryMapper;
import org.bme.micro_futar.orders.repositories.LocationCountryRepository;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
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
class LocationCountryServiceTests {

    @Mock
    private LocationCountryRepository locationCountryRepository;

    @Mock
    private LocationCountryMapper locationCountryMapper;

    @InjectMocks
    private LocationCountryService locationCountryService;

    private LocationCountry country1;
    private LocationCountry country2;
    private LocationCountryDTO countryDTO1;
    private LocationCountryDTO countryDTO2;

    @BeforeEach
    void setUp() {
        country1 = new LocationCountry();
        country1.setId(1L);
        country1.setName("Hungary");
        country1.setRegionId(1L);

        country2 = new LocationCountry();
        country2.setId(2L);
        country2.setName("Austria");
        country2.setRegionId(2L);

        countryDTO1 = LocationCountryDTO.builder()
                .id(1L)
                .name("Hungary")
                .regionId(1L)
                .build();

        countryDTO2 = LocationCountryDTO.builder()
                .id(2L)
                .name("Austria")
                .regionId(2L)
                .build();
    }

    @Test
    void testGetAllCountries() {
        // Given
        List<LocationCountry> countries = Arrays.asList(country1, country2);
        when(locationCountryRepository.findAll()).thenReturn(countries);
        when(locationCountryMapper.toDTO(country1)).thenReturn(countryDTO1);
        when(locationCountryMapper.toDTO(country2)).thenReturn(countryDTO2);

        // When
        List<LocationCountryDTO> result = locationCountryService.getAllCountries();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Hungary");
        assertThat(result.get(1).getName()).isEqualTo("Austria");
        verify(locationCountryRepository).findAll();
        verify(locationCountryMapper, times(2)).toDTO(any(LocationCountry.class));
    }

    @Test
    void testGetAllCountries_EmptyList() {
        // Given
        when(locationCountryRepository.findAll()).thenReturn(List.of());

        // When
        List<LocationCountryDTO> result = locationCountryService.getAllCountries();

        // Then
        assertThat(result).isEmpty();
        verify(locationCountryRepository).findAll();
    }

    @Test
    void testGetCountryById_Found() {
        // Given
        when(locationCountryRepository.findById(1L)).thenReturn(Optional.of(country1));
        when(locationCountryMapper.toDTO(country1)).thenReturn(countryDTO1);

        // When
        Optional<LocationCountryDTO> result = locationCountryService.getCountryById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Hungary");
        verify(locationCountryRepository).findById(1L);
        verify(locationCountryMapper).toDTO(country1);
    }

    @Test
    void testGetCountryById_NotFound() {
        // Given
        when(locationCountryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<LocationCountryDTO> result = locationCountryService.getCountryById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(locationCountryRepository).findById(999L);
        verify(locationCountryMapper, never()).toDTO(any());
    }

    @Test
    void testSaveLocationCountry() {
        // Given
        when(locationCountryMapper.toEntity(countryDTO1)).thenReturn(country1);
        when(locationCountryRepository.save(country1)).thenReturn(country1);

        // When
        LocationCountry result = locationCountryService.saveLocationCountry(countryDTO1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Hungary");
        verify(locationCountryMapper).toEntity(countryDTO1);
        verify(locationCountryRepository).save(country1);
    }
}


package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.orders.mappers.LocationCityMapper;
import org.bme.micro_futar.orders.repositories.LocationCityRepository;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
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
class LocationCityServiceTests {

    @Mock
    private LocationCityRepository locationCityRepository;

    @Mock
    private LocationCityMapper locationCityMapper;

    @InjectMocks
    private LocationCityService locationCityService;

    private LocationCity city1;
    private LocationCity city2;
    private LocationCityDTO cityDTO1;
    private LocationCityDTO cityDTO2;

    @BeforeEach
    void setUp() {
        city1 = new LocationCity();
        city1.setId(1L);
        city1.setName("Budapest");
        city1.setCountryId(1L);

        city2 = new LocationCity();
        city2.setId(2L);
        city2.setName("Vienna");
        city2.setCountryId(2L);

        cityDTO1 = LocationCityDTO.builder()
                .id(1L)
                .name("Budapest")
                .countryId(1L)
                .build();

        cityDTO2 = LocationCityDTO.builder()
                .id(2L)
                .name("Vienna")
                .countryId(2L)
                .build();
    }

    @Test
    void testGetAllCities() {
        List<LocationCity> cities = Arrays.asList(city1, city2);
        when(locationCityRepository.findAll()).thenReturn(cities);
        when(locationCityMapper.toDTO(city1)).thenReturn(cityDTO1);
        when(locationCityMapper.toDTO(city2)).thenReturn(cityDTO2);

        List<LocationCityDTO> result = locationCityService.getAllCities();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Budapest");
        assertThat(result.get(1).getName()).isEqualTo("Vienna");
        verify(locationCityRepository).findAll();
        verify(locationCityMapper, times(2)).toDTO(any(LocationCity.class));
    }

    @Test
    void testGetAllCities_EmptyList() {
        when(locationCityRepository.findAll()).thenReturn(List.of());

        List<LocationCityDTO> result = locationCityService.getAllCities();

        assertThat(result).isEmpty();
        verify(locationCityRepository).findAll();
    }

    @Test
    void testGetCityById_Found() {
        when(locationCityRepository.findById(1L)).thenReturn(Optional.of(city1));
        when(locationCityMapper.toDTO(city1)).thenReturn(cityDTO1);

        Optional<LocationCityDTO> result = locationCityService.getCityById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Budapest");
        verify(locationCityRepository).findById(1L);
        verify(locationCityMapper).toDTO(city1);
    }

    @Test
    void testGetCityById_NotFound() {
        when(locationCityRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<LocationCityDTO> result = locationCityService.getCityById(999L);

        assertThat(result).isEmpty();
        verify(locationCityRepository).findById(999L);
        verify(locationCityMapper, never()).toDTO(any());
    }

    @Test
    void testSaveLocationCity() {
        when(locationCityMapper.toEntity(cityDTO1)).thenReturn(city1);
        when(locationCityRepository.save(city1)).thenReturn(city1);

        LocationCity result = locationCityService.saveLocationCity(cityDTO1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Budapest");
        verify(locationCityMapper).toEntity(cityDTO1);
        verify(locationCityRepository).save(city1);
    }
}

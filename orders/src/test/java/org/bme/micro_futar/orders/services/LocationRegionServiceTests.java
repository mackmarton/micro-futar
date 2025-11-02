package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.LocationRegion;
import org.bme.micro_futar.orders.mappers.LocationRegionMapper;
import org.bme.micro_futar.orders.repositories.LocationRegionRepository;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
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
class LocationRegionServiceTests {

    @Mock
    private LocationRegionRepository locationRegionRepository;

    @Mock
    private LocationRegionMapper locationRegionMapper;

    @InjectMocks
    private LocationRegionService locationRegionService;

    private LocationRegion region1;
    private LocationRegion region2;
    private LocationRegionDTO regionDTO1;
    private LocationRegionDTO regionDTO2;

    @BeforeEach
    void setUp() {
        region1 = new LocationRegion();
        region1.setId(1L);
        region1.setName("Central Europe");

        region2 = new LocationRegion();
        region2.setId(2L);
        region2.setName("Western Europe");

        regionDTO1 = LocationRegionDTO.builder()
                .id(1L)
                .name("Central Europe")
                .build();

        regionDTO2 = LocationRegionDTO.builder()
                .id(2L)
                .name("Western Europe")
                .build();
    }

    @Test
    void testGetAllRegions() {
        List<LocationRegion> regions = Arrays.asList(region1, region2);
        when(locationRegionRepository.findAll()).thenReturn(regions);
        when(locationRegionMapper.toDTO(region1)).thenReturn(regionDTO1);
        when(locationRegionMapper.toDTO(region2)).thenReturn(regionDTO2);

        List<LocationRegionDTO> result = locationRegionService.getAllRegions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Central Europe");
        assertThat(result.get(1).getName()).isEqualTo("Western Europe");
        verify(locationRegionRepository).findAll();
        verify(locationRegionMapper, times(2)).toDTO(any(LocationRegion.class));
    }

    @Test
    void testGetAllRegions_EmptyList() {
        when(locationRegionRepository.findAll()).thenReturn(List.of());

        List<LocationRegionDTO> result = locationRegionService.getAllRegions();

        assertThat(result).isEmpty();
        verify(locationRegionRepository).findAll();
    }

    @Test
    void testGetRegionById_Found() {
        when(locationRegionRepository.findById(1L)).thenReturn(Optional.of(region1));
        when(locationRegionMapper.toDTO(region1)).thenReturn(regionDTO1);

        Optional<LocationRegionDTO> result = locationRegionService.getRegionById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Central Europe");
        verify(locationRegionRepository).findById(1L);
        verify(locationRegionMapper).toDTO(region1);
    }

    @Test
    void testGetRegionById_NotFound() {
        when(locationRegionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<LocationRegionDTO> result = locationRegionService.getRegionById(999L);

        assertThat(result).isEmpty();
        verify(locationRegionRepository).findById(999L);
        verify(locationRegionMapper, never()).toDTO(any());
    }

    @Test
    void testSaveLocationRegion() {
        when(locationRegionMapper.toEntity(regionDTO1)).thenReturn(region1);
        when(locationRegionRepository.save(region1)).thenReturn(region1);

        LocationRegion result = locationRegionService.saveLocationRegion(regionDTO1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Central Europe");
        verify(locationRegionMapper).toEntity(regionDTO1);
        verify(locationRegionRepository).save(region1);
    }
}

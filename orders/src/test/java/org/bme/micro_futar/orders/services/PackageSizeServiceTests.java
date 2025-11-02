package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.PackageSize;
import org.bme.micro_futar.orders.mappers.PackageSizeMapper;
import org.bme.micro_futar.orders.repositories.PackageSizeRepository;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
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
class PackageSizeServiceTests {

    @Mock
    private PackageSizeRepository packageSizeRepository;

    @Mock
    private PackageSizeMapper packageSizeMapper;

    @InjectMocks
    private PackageSizeService packageSizeService;

    private PackageSize packageSize1;
    private PackageSize packageSize2;
    private PackageSizeDTO packageSizeDTO1;
    private PackageSizeDTO packageSizeDTO2;

    @BeforeEach
    void setUp() {
        packageSize1 = new PackageSize();
        packageSize1.setId(1L);
        packageSize1.setName("Small");
        packageSize1.setMaxLength(30.0);

        packageSize2 = new PackageSize();
        packageSize2.setId(2L);
        packageSize2.setName("Medium");
        packageSize2.setMaxLength(60.0);

        packageSizeDTO1 = PackageSizeDTO.builder()
                .id(1L)
                .name("Small")
                .maxLength(30.0)
                .build();

        packageSizeDTO2 = PackageSizeDTO.builder()
                .id(2L)
                .name("Medium")
                .maxLength(60.0)
                .build();
    }

    @Test
    void testGetAllPackageSizes() {
        List<PackageSize> packageSizes = Arrays.asList(packageSize1, packageSize2);
        when(packageSizeRepository.findAll()).thenReturn(packageSizes);
        when(packageSizeMapper.toDTO(packageSize1)).thenReturn(packageSizeDTO1);
        when(packageSizeMapper.toDTO(packageSize2)).thenReturn(packageSizeDTO2);

        List<PackageSizeDTO> result = packageSizeService.getAllPackageSizes();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Small");
        assertThat(result.get(0).getMaxLength()).isEqualTo(30.0);
        assertThat(result.get(1).getName()).isEqualTo("Medium");
        assertThat(result.get(1).getMaxLength()).isEqualTo(60.0);
        verify(packageSizeRepository).findAll();
        verify(packageSizeMapper, times(2)).toDTO(any(PackageSize.class));
    }

    @Test
    void testGetAllPackageSizes_EmptyList() {
        when(packageSizeRepository.findAll()).thenReturn(List.of());

        List<PackageSizeDTO> result = packageSizeService.getAllPackageSizes();

        assertThat(result).isEmpty();
        verify(packageSizeRepository).findAll();
    }

    @Test
    void testGetPackageSizeById_Found() {
        when(packageSizeRepository.findById(1L)).thenReturn(Optional.of(packageSize1));
        when(packageSizeMapper.toDTO(packageSize1)).thenReturn(packageSizeDTO1);

        Optional<PackageSizeDTO> result = packageSizeService.getPackageSizeById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Small");
        assertThat(result.get().getMaxLength()).isEqualTo(30.0);
        verify(packageSizeRepository).findById(1L);
        verify(packageSizeMapper).toDTO(packageSize1);
    }

    @Test
    void testGetPackageSizeById_NotFound() {
        when(packageSizeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<PackageSizeDTO> result = packageSizeService.getPackageSizeById(999L);

        assertThat(result).isEmpty();
        verify(packageSizeRepository).findById(999L);
        verify(packageSizeMapper, never()).toDTO(any());
    }

    @Test
    void testSavePackageSize() {
        when(packageSizeMapper.toEntity(packageSizeDTO1)).thenReturn(packageSize1);
        when(packageSizeRepository.save(packageSize1)).thenReturn(packageSize1);

        PackageSize result = packageSizeService.savePackageSize(packageSizeDTO1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Small");
        assertThat(result.getMaxLength()).isEqualTo(30.0);
        verify(packageSizeMapper).toEntity(packageSizeDTO1);
        verify(packageSizeRepository).save(packageSize1);
    }
}

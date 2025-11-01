package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.EnableTestContainers;
import org.bme.micro_futar.orders.services.PackageSizeService;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableTestContainers
class PackageSizeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PackageSizeService packageSizeService;

    @Test
    void testGetAllPackageSizes() throws Exception {
        PackageSizeDTO size1 = PackageSizeDTO.builder()
                .id(1L)
                .name("Small")
                .maxLength(2.0)
                .build();

        PackageSizeDTO size2 = PackageSizeDTO.builder()
                .id(2L)
                .name("Medium")
                .maxLength(5.0)
                .build();

        List<PackageSizeDTO> sizes = Arrays.asList(size1, size2);

        when(packageSizeService.getAllPackageSizes()).thenReturn(sizes);

        mockMvc.perform(get("/api/package-sizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Small"))
                .andExpect(jsonPath("$[0].maxLength").value(2.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Medium"))
                .andExpect(jsonPath("$[1].maxLength").value(5.0));
    }

    @Test
    void testGetPackageSizeById_Found() throws Exception {
        PackageSizeDTO size = PackageSizeDTO.builder()
                .id(1L)
                .name("Small")
                .maxLength(2.0)
                .build();

        when(packageSizeService.getPackageSizeById(1L)).thenReturn(Optional.of(size));

        mockMvc.perform(get("/api/package-sizes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Small"))
                .andExpect(jsonPath("$.maxLength").value(2.0));
    }

    @Test
    void testGetPackageSizeById_NotFound() throws Exception {
        when(packageSizeService.getPackageSizeById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/package-sizes/999"))
                .andExpect(status().isNotFound());
    }
}

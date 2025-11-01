package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.EnableTestContainers;
import org.bme.micro_futar.orders.services.LocationRegionService;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
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
class LocationRegionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationRegionService locationRegionService;

    @Test
    void testGetAllRegions() throws Exception {
        LocationRegionDTO region1 = LocationRegionDTO.builder()
                .id(1L)
                .name("Central Hungary")
                .build();

        LocationRegionDTO region2 = LocationRegionDTO.builder()
                .id(2L)
                .name("Lower Austria")
                .build();

        List<LocationRegionDTO> regions = Arrays.asList(region1, region2);

        when(locationRegionService.getAllRegions()).thenReturn(regions);

        mockMvc.perform(get("/api/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Central Hungary"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Lower Austria"));
    }

    @Test
    void testGetRegionById_Found() throws Exception {
        LocationRegionDTO region = LocationRegionDTO.builder()
                .id(1L)
                .name("Central Hungary")
                .build();

        when(locationRegionService.getRegionById(1L)).thenReturn(Optional.of(region));

        mockMvc.perform(get("/api/regions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Central Hungary"));
    }

    @Test
    void testGetRegionById_NotFound() throws Exception {
        when(locationRegionService.getRegionById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/regions/999"))
                .andExpect(status().isNotFound());
    }
}

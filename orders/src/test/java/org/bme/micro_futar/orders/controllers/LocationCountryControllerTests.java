package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.EnableTestContainers;
import org.bme.micro_futar.orders.services.LocationCountryService;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
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
class LocationCountryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationCountryService locationCountryService;

    @Test
    void testGetAllCountries() throws Exception {
        LocationCountryDTO country1 = LocationCountryDTO.builder()
                .id(1L)
                .name("Hungary")
                .regionId(1L)
                .build();

        LocationCountryDTO country2 = LocationCountryDTO.builder()
                .id(2L)
                .name("Austria")
                .regionId(2L)
                .build();

        List<LocationCountryDTO> countries = Arrays.asList(country1, country2);

        when(locationCountryService.getAllCountries()).thenReturn(countries);

        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Hungary"))
                .andExpect(jsonPath("$[0].regionId").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Austria"))
                .andExpect(jsonPath("$[1].regionId").value(2));
    }

    @Test
    void testGetCountryById_Found() throws Exception {
        LocationCountryDTO country = LocationCountryDTO.builder()
                .id(1L)
                .name("Hungary")
                .regionId(1L)
                .build();

        when(locationCountryService.getCountryById(1L)).thenReturn(Optional.of(country));

        mockMvc.perform(get("/api/countries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hungary"))
                .andExpect(jsonPath("$.regionId").value(1));
    }

    @Test
    void testGetCountryById_NotFound() throws Exception {
        when(locationCountryService.getCountryById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/countries/999"))
                .andExpect(status().isNotFound());
    }
}

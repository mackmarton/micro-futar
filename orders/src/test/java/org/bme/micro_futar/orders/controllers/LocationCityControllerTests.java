package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.EnableTestContainers;
import org.bme.micro_futar.orders.services.LocationCityService;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
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
class LocationCityControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationCityService locationCityService;

    @Test
    void testGetAllCities() throws Exception {
        LocationCityDTO city1 = LocationCityDTO.builder()
                .id(1L)
                .name("Budapest")
                .countryId(1L)
                .build();

        LocationCityDTO city2 = LocationCityDTO.builder()
                .id(2L)
                .name("Vienna")
                .countryId(2L)
                .build();

        List<LocationCityDTO> cities = Arrays.asList(city1, city2);

        when(locationCityService.getAllCities()).thenReturn(cities);

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Budapest"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Vienna"));
    }

    @Test
    void testGetCityById_Found() throws Exception {
        LocationCityDTO city = LocationCityDTO.builder()
                .id(1L)
                .name("Budapest")
                .countryId(1L)
                .build();

        when(locationCityService.getCityById(1L)).thenReturn(Optional.of(city));

        mockMvc.perform(get("/api/cities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Budapest"))
                .andExpect(jsonPath("$.countryId").value(1));
    }

    @Test
    void testGetCityById_NotFound() throws Exception {
        when(locationCityService.getCityById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cities/999"))
                .andExpect(status().isNotFound());
    }
}

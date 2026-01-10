package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.EnableTestContainers;
import org.bme.micro_futar.orders.services.CountryPriceService;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
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
class CountryPriceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CountryPriceService countryPriceService;

    @Test
    void testGetAllCountryPrices() throws Exception {
        CountryPriceDTO price1 = CountryPriceDTO.builder()
                .id(1L)
                .originCountryId(1L)
                .destinationCountryId(2L)
                .packageSizeId(1L)
                .build();

        CountryPriceDTO price2 = CountryPriceDTO.builder()
                .id(2L)
                .originCountryId(1L)
                .destinationCountryId(3L)
                .packageSizeId(2L)
                .build();

        List<CountryPriceDTO> prices = Arrays.asList(price1, price2);

        when(countryPriceService.getAllCountryPrices()).thenReturn(prices);

        mockMvc.perform(get("/api/country-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].originCountryId").value(1))
                .andExpect(jsonPath("$[0].destinationCountryId").value(2))
                .andExpect(jsonPath("$[0].packageSizeId").value(1))
                .andExpect(jsonPath("$[0].price").value(25.99))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].originCountryId").value(1))
                .andExpect(jsonPath("$[1].destinationCountryId").value(3))
                .andExpect(jsonPath("$[1].packageSizeId").value(2))
                .andExpect(jsonPath("$[1].price").value(45.99));
    }

    @Test
    void testGetAllCountryPrices_EmptyList() throws Exception {
        when(countryPriceService.getAllCountryPrices()).thenReturn(List.of());

        mockMvc.perform(get("/api/country-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetCountryPriceById_Found() throws Exception {
        CountryPriceDTO price = CountryPriceDTO.builder()
                .id(1L)
                .originCountryId(1L)
                .destinationCountryId(2L)
                .packageSizeId(1L)
                .build();

        when(countryPriceService.getCountryPriceById(1L)).thenReturn(Optional.of(price));

        mockMvc.perform(get("/api/country-prices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originCountryId").value(1))
                .andExpect(jsonPath("$.destinationCountryId").value(2))
                .andExpect(jsonPath("$.packageSizeId").value(1))
                .andExpect(jsonPath("$.price").value(25.99));
    }

    @Test
    void testGetCountryPriceById_NotFound() throws Exception {
        when(countryPriceService.getCountryPriceById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/country-prices/999"))
                .andExpect(status().isNotFound());
    }
}


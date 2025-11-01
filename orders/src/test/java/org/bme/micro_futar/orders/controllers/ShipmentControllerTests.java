package org.bme.micro_futar.orders.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.orders.EnableTestContainers;
import org.bme.micro_futar.orders.services.CountryPriceService;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableTestContainers
class ShipmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CountryPriceService countryPriceService;

    @Test
    void testNewShipment() throws Exception {
        ShipmentDTO shipmentDTO = ShipmentDTO.builder()
                .senderName("Test Sender")
                .senderEmail("sender@test.com")
                .senderPhone("123456789")
                .senderLocationCountryId(1L)
                .senderZip("1234")
                .senderLocationCityId(1L)
                .senderAddress("1 Test Street")
                .recipientName("Test Recipient")
                .recipientEmail("recipient@test.com")
                .recipientPhone("987654321")
                .recipientLocationCountryId(2L)
                .recipientZip("5678")
                .recipientLocationCityId(2L)
                .recipientAddress("2 Test Avenue")
                .packageSizeId(3L)
                .build();

        when(countryPriceService.findPriceByCountriesAndSize(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(CountryPriceDTO.builder()
                        .id(1L)
                        .originCountryId(1L)
                        .destinationCountryId(2L)
                        .packageSizeId(3L)
                        .price(100.0)
                        .build()));

        mockMvc.perform(post("/api/shipments/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(100.0));
    }
}

package org.bme.micro_futar.orders.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.orders.EnableTestContainers;
import org.bme.micro_futar.orders.services.CountryPriceService;
import org.bme.micro_futar.orders.services.ShipmentService;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
    private CountryPriceService countryPriceService;

    @MockitoBean
    private ShipmentService shipmentService;

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

        ShipmentDTO savedShipment = ShipmentDTO.builder()
                .id(1L)
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
                .price(100.0)
                .build();

        when(countryPriceService.findPriceByCountriesAndSize(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(CountryPriceDTO.builder()
                        .id(1L)
                        .originCountryId(1L)
                        .destinationCountryId(2L)
                        .packageSizeId(3L)
                        .minPrice(10.0)
                        .maxPrice(15.0)
                        .build()));

        when(shipmentService.newShipment(shipmentDTO)).thenReturn(savedShipment);

        mockMvc.perform(post("/api/shipments/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(100.0))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testNewShipmentWithDifferentCountries() throws Exception {
        ShipmentDTO shipmentDTO = ShipmentDTO.builder()
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("111222333")
                .senderLocationCountryId(3L)
                .senderZip("4321")
                .senderLocationCityId(5L)
                .senderAddress("10 Main Street")
                .recipientName("Jane Smith")
                .recipientEmail("jane@example.com")
                .recipientPhone("444555666")
                .recipientLocationCountryId(4L)
                .recipientZip("9876")
                .recipientLocationCityId(6L)
                .recipientAddress("20 Park Avenue")
                .packageSizeId(2L)
                .build();

        ShipmentDTO savedShipment = ShipmentDTO.builder()
                .id(2L)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("111222333")
                .senderLocationCountryId(3L)
                .senderZip("4321")
                .senderLocationCityId(5L)
                .senderAddress("10 Main Street")
                .recipientName("Jane Smith")
                .recipientEmail("jane@example.com")
                .recipientPhone("444555666")
                .recipientLocationCountryId(4L)
                .recipientZip("9876")
                .recipientLocationCityId(6L)
                .recipientAddress("20 Park Avenue")
                .packageSizeId(2L)
                .price(200.0)
                .build();

        when(countryPriceService.findPriceByCountriesAndSize(3L, 4L, 2L))
                .thenReturn(Optional.of(CountryPriceDTO.builder()
                        .id(2L)
                        .originCountryId(3L)
                        .destinationCountryId(4L)
                        .packageSizeId(2L)
                        .minPrice(10.0)
                        .maxPrice(15.0)
                        .build()));

        when(shipmentService.newShipment(shipmentDTO)).thenReturn(savedShipment);

        mockMvc.perform(post("/api/shipments/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.price").value(200.0))
                .andExpect(jsonPath("$.senderName").value("John Doe"))
                .andExpect(jsonPath("$.recipientName").value("Jane Smith"));
    }

    @Test
    void testConfirmShipment() throws Exception {
        ShipmentDTO confirmedShipment = ShipmentDTO.builder()
                .id(1L)
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
                .price(100.0)
                .build();

        when(shipmentService.confirm(1L)).thenReturn(confirmedShipment);

        mockMvc.perform(post("/api/shipments/confirm/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(100.0))
                .andExpect(jsonPath("$.senderName").value("Test Sender"));
    }

    @Test
    void testConfirmShipment_DifferentId() throws Exception {
        ShipmentDTO confirmedShipment = ShipmentDTO.builder()
                .id(5L)
                .senderName("Another Sender")
                .senderEmail("another@test.com")
                .senderPhone("555666777")
                .senderLocationCountryId(2L)
                .senderZip("7890")
                .senderLocationCityId(3L)
                .senderAddress("5 Oak Street")
                .recipientName("Another Recipient")
                .recipientEmail("recipient2@test.com")
                .recipientPhone("888999000")
                .recipientLocationCountryId(3L)
                .recipientZip("1111")
                .recipientLocationCityId(4L)
                .recipientAddress("6 Elm Avenue")
                .packageSizeId(1L)
                .price(75.0)
                .build();

        when(shipmentService.confirm(5L)).thenReturn(confirmedShipment);

        mockMvc.perform(post("/api/shipments/confirm/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.price").value(75.0))
                .andExpect(jsonPath("$.senderName").value("Another Sender"))
                .andExpect(jsonPath("$.recipientName").value("Another Recipient"));
    }
}

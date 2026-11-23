package org.bme.micro_futar.logistics.exceptions;

import org.bme.micro_futar.logistics.controllers.CourierController;
import org.bme.micro_futar.logistics.services.CourierService;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

    @Test
    void invalidReferenceIsReturnedAs400WithFieldSpecificMessage() throws Exception {
        CourierService courierService = mock(CourierService.class);
        when(courierService.createCourier(any(CourierDTO.class))).thenThrow(
                new InvalidReferenceException("depoId", "Depo 42 (depoId) does not exist or has been deleted"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CourierController(courierService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/couriers").contentType(MediaType.APPLICATION_JSON).content("{\"depoId\":42}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Depo 42 (depoId) does not exist or has been deleted"))
                .andExpect(jsonPath("$.field").value("depoId"));
    }
}

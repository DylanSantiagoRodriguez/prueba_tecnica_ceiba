package com.bikerental.infrastructure.web.controller;

import com.bikerental.application.dto.response.RentalResponse;
import com.bikerental.domain.exception.BikeNotAvailableException;
import com.bikerental.domain.exception.RentalAlreadyFinishedException;
import com.bikerental.domain.exception.RentalNotFoundException;
import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.port.in.FinishRentalUseCase;
import com.bikerental.domain.port.in.StartRentalUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RentalController.class)
class RentalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private StartRentalUseCase startRentalUseCase;
    @MockBean private FinishRentalUseCase finishRentalUseCase;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    private RentalResponse activeRentalResponse() {
        return new RentalResponse(1L, "BIC-001", "Ana", NOW, null, null, null, false, false);
    }

    private RentalResponse finishedRentalResponse() {
        return new RentalResponse(1L, "BIC-001", "Ana", NOW, NOW.plusHours(2),
                2, new BigDecimal("7000"), false, true);
    }

    @Test
    void iniciar_alquiler_retorna_201() throws Exception {
        when(startRentalUseCase.startRental(any())).thenReturn(activeRentalResponse());

        String body = objectMapper.writeValueAsString(
                Map.of("bikeCode", "BIC-001", "customerName", "Ana", "estimatedHours", 2));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bikeCode").value("BIC-001"))
                .andExpect(jsonPath("$.finished").value(false));
    }

    @Test
    void iniciar_alquiler_bicicleta_alquilada_retorna_409() throws Exception {
        when(startRentalUseCase.startRental(any()))
                .thenThrow(new BikeNotAvailableException("BIC-001", BikeStatus.ALQUILADA));

        String body = objectMapper.writeValueAsString(
                Map.of("bikeCode", "BIC-001", "customerName", "Ana", "estimatedHours", 2));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void finalizar_alquiler_retorna_200_con_costo_calculado() throws Exception {
        when(finishRentalUseCase.finishRental(eq(1L), any())).thenReturn(finishedRentalResponse());

        String body = objectMapper.writeValueAsString(
                Map.of("returnTime", "2026-01-01T12:00:00"));

        mockMvc.perform(patch("/api/rentals/1/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finished").value(true))
                .andExpect(jsonPath("$.totalCost").value(7000));
    }

    @Test
    void finalizar_alquiler_inexistente_retorna_404() throws Exception {
        when(finishRentalUseCase.finishRental(eq(99L), any()))
                .thenThrow(new RentalNotFoundException(99L));

        String body = objectMapper.writeValueAsString(
                Map.of("returnTime", "2026-01-01T12:00:00"));

        mockMvc.perform(patch("/api/rentals/99/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void finalizar_alquiler_ya_terminado_retorna_409() throws Exception {
        when(finishRentalUseCase.finishRental(eq(1L), any()))
                .thenThrow(new RentalAlreadyFinishedException(1L));

        String body = objectMapper.writeValueAsString(
                Map.of("returnTime", "2026-01-01T12:00:00"));

        mockMvc.perform(patch("/api/rentals/1/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}

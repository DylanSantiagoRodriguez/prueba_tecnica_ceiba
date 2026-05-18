package com.bikerental.infrastructure.web.controller;

import com.bikerental.application.dto.response.BikeResponse;
import com.bikerental.application.dto.response.RentalResponse;
import com.bikerental.domain.exception.BikeNotFoundException;
import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import com.bikerental.domain.port.in.GetAllBikesUseCase;
import com.bikerental.domain.port.in.GetAvailableBikesUseCase;
import com.bikerental.domain.port.in.GetRentalHistoryUseCase;
import com.bikerental.domain.port.in.RegisterBikeUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BikeController.class)
class BikeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RegisterBikeUseCase registerBikeUseCase;
    @MockBean private GetAllBikesUseCase getAllBikesUseCase;
    @MockBean private GetAvailableBikesUseCase getAvailableBikesUseCase;
    @MockBean private GetRentalHistoryUseCase getRentalHistoryUseCase;

    @Test
    void registrar_bicicleta_retorna_201() throws Exception {
        BikeResponse response = new BikeResponse("BIC-001", BikeType.URBANA, BikeStatus.DISPONIBLE);
        when(registerBikeUseCase.register(any())).thenReturn(response);

        String body = objectMapper.writeValueAsString(
                Map.of("code", "BIC-001", "type", "URBANA", "status", "DISPONIBLE"));

        mockMvc.perform(post("/api/bikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("BIC-001"))
                .andExpect(jsonPath("$.type").value("URBANA"));
    }

    @Test
    void registrar_bicicleta_sin_codigo_retorna_400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("type", "URBANA", "status", "DISPONIBLE"));

        mockMvc.perform(post("/api/bikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultar_disponibles_retorna_200_con_lista() throws Exception {
        when(getAvailableBikesUseCase.getAvailableBikes(null))
                .thenReturn(List.of(new BikeResponse("BIC-001", BikeType.URBANA, BikeStatus.DISPONIBLE)));

        mockMvc.perform(get("/api/bikes/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("BIC-001"));
    }

    @Test
    void consultar_disponibles_con_filtro_tipo_valido() throws Exception {
        when(getAvailableBikesUseCase.getAvailableBikes(BikeType.MONTANA))
                .thenReturn(List.of(new BikeResponse("BIC-002", BikeType.MONTANA, BikeStatus.DISPONIBLE)));

        mockMvc.perform(get("/api/bikes/available").param("type", "MONTANA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("MONTANA"));
    }

    @Test
    void historial_bicicleta_inexistente_retorna_404() throws Exception {
        when(getRentalHistoryUseCase.getRentalHistory("BIC-999"))
                .thenThrow(new BikeNotFoundException("BIC-999"));

        mockMvc.perform(get("/api/bikes/BIC-999/history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void historial_bicicleta_existente_retorna_200() throws Exception {
        when(getRentalHistoryUseCase.getRentalHistory("BIC-001"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/bikes/BIC-001/history"))
                .andExpect(status().isOk());
    }
}

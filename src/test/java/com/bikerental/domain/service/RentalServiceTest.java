package com.bikerental.domain.service;

import com.bikerental.application.dto.request.FinishRentalRequest;
import com.bikerental.application.dto.request.RegisterBikeRequest;
import com.bikerental.application.dto.request.StartRentalRequest;
import com.bikerental.application.dto.response.BikeResponse;
import com.bikerental.application.dto.response.RentalResponse;
import com.bikerental.domain.exception.BikeNotAvailableException;
import com.bikerental.domain.exception.BikeNotFoundException;
import com.bikerental.domain.exception.RentalAlreadyFinishedException;
import com.bikerental.domain.exception.RentalNotFoundException;
import com.bikerental.domain.model.Bike;
import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import com.bikerental.domain.model.Rental;
import com.bikerental.domain.port.out.BikeRepository;
import com.bikerental.domain.port.out.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock private BikeRepository bikeRepository;
    @Mock private RentalRepository rentalRepository;
    @InjectMocks private RentalService service;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);
    private Bike disponibleBike;
    private Bike alquiladaBike;
    private Rental activeRental;
    private Rental finishedRental;

    @BeforeEach
    void setUp() {
        disponibleBike = new Bike("BIC-001", BikeType.URBANA, BikeStatus.DISPONIBLE);
        alquiladaBike = new Bike("BIC-002", BikeType.MONTANA, BikeStatus.ALQUILADA);
        activeRental = new Rental(1L, "BIC-001", "Ana", NOW, 2, null, null, false);
        finishedRental = new Rental(2L, "BIC-001", "Luis", NOW, 2,
                NOW.plusHours(2), java.math.BigDecimal.valueOf(7000), false);
    }

    @Test
    void iniciar_alquiler_bicicleta_disponible_exitoso() {
        when(bikeRepository.findByCode("BIC-001")).thenReturn(Optional.of(disponibleBike));
        when(bikeRepository.save(any())).thenReturn(disponibleBike);
        Rental savedRental = new Rental(1L, "BIC-001", "Ana", NOW, 2, null, null, false);
        when(rentalRepository.save(any())).thenReturn(savedRental);

        StartRentalRequest req = new StartRentalRequest();
        req.setBikeCode("BIC-001");
        req.setCustomerName("Ana");
        req.setEstimatedHours(2);

        RentalResponse resp = service.startRental(req);

        assertNotNull(resp);
        assertEquals("BIC-001", resp.getBikeCode());
        assertFalse(resp.isFinished());
        verify(bikeRepository).save(any());
        verify(rentalRepository).save(any());
    }

    @Test
    void iniciar_alquiler_bicicleta_no_existe_lanza_BikeNotFoundException() {
        when(bikeRepository.findByCode("BIC-999")).thenReturn(Optional.empty());

        StartRentalRequest req = new StartRentalRequest();
        req.setBikeCode("BIC-999");
        req.setCustomerName("Ana");
        req.setEstimatedHours(2);

        assertThrows(BikeNotFoundException.class, () -> service.startRental(req));
    }

    @Test
    void iniciar_alquiler_bicicleta_no_disponible_lanza_BikeNotAvailableException() {
        when(bikeRepository.findByCode("BIC-002")).thenReturn(Optional.of(alquiladaBike));

        StartRentalRequest req = new StartRentalRequest();
        req.setBikeCode("BIC-002");
        req.setCustomerName("Ana");
        req.setEstimatedHours(2);

        assertThrows(BikeNotAvailableException.class, () -> service.startRental(req));
    }

    @Test
    void finalizar_alquiler_existente_exitoso() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(activeRental));
        when(bikeRepository.findByCode("BIC-001")).thenReturn(Optional.of(disponibleBike));
        when(rentalRepository.save(any())).thenReturn(activeRental);
        when(bikeRepository.save(any())).thenReturn(disponibleBike);

        FinishRentalRequest req = new FinishRentalRequest();
        req.setReturnTime(NOW.plusHours(2));

        RentalResponse resp = service.finishRental(1L, req);

        assertNotNull(resp);
        assertTrue(resp.isFinished());
        verify(rentalRepository).save(any());
        verify(bikeRepository).save(any());
    }

    @Test
    void finalizar_alquiler_inexistente_lanza_RentalNotFoundException() {
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        FinishRentalRequest req = new FinishRentalRequest();
        req.setReturnTime(NOW.plusHours(2));

        assertThrows(RentalNotFoundException.class, () -> service.finishRental(99L, req));
    }

    @Test
    void finalizar_alquiler_ya_terminado_lanza_RentalAlreadyFinishedException() {
        when(rentalRepository.findById(2L)).thenReturn(Optional.of(finishedRental));

        FinishRentalRequest req = new FinishRentalRequest();
        req.setReturnTime(NOW.plusHours(3));

        assertThrows(RentalAlreadyFinishedException.class, () -> service.finishRental(2L, req));
    }

    @Test
    void consultar_disponibles_sin_filtro_retorna_todas() {
        when(bikeRepository.findByStatus(BikeStatus.DISPONIBLE))
                .thenReturn(List.of(disponibleBike));

        List<BikeResponse> result = service.getAvailableBikes(null);

        assertEquals(1, result.size());
        assertEquals("BIC-001", result.get(0).getCode());
    }

    @Test
    void consultar_disponibles_con_filtro_tipo_retorna_filtradas() {
        when(bikeRepository.findByStatusAndType(BikeStatus.DISPONIBLE, BikeType.URBANA))
                .thenReturn(List.of(disponibleBike));

        List<BikeResponse> result = service.getAvailableBikes(BikeType.URBANA);

        assertEquals(1, result.size());
        assertEquals(BikeType.URBANA, result.get(0).getType());
    }

    @Test
    void historial_bicicleta_retorna_lista_de_alquileres() {
        when(rentalRepository.findByBikeCode("BIC-001"))
                .thenReturn(List.of(activeRental, finishedRental));

        List<RentalResponse> result = service.getRentalHistory("BIC-001");

        assertEquals(2, result.size());
    }
}

package com.bikerental.domain;

import com.bikerental.domain.model.Bike;
import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import com.bikerental.domain.model.Rental;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RentalDomainTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 1, 1, 10, 0);

    // ── RN-02: costo base redondeado al alza ───────────────────────────────

    private void assertCost(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(expected),
            "Esperado " + expected + " pero fue " + actual);
    }

    @Test
    void costo_bicicleta_urbana_1h10min_cobra_2h() {
        Rental rental = new Rental("BIC-001", "Ana", BASE, 180);
        rental.finish(BASE.plusMinutes(70), BikeType.URBANA.getHourlyRate());

        assertCost(new BigDecimal("7000"), rental.getTotalCost());
        assertFalse(rental.isHasPenalty());
    }

    @Test
    void costo_exactamente_2_horas_cobra_2h() {
        Rental rental = new Rental("BIC-001", "Ana", BASE, 180);
        rental.finish(BASE.plusHours(2), BikeType.URBANA.getHourlyRate());

        assertCost(new BigDecimal("7000"), rental.getTotalCost());
        assertFalse(rental.isHasPenalty());
    }

    @Test
    void costo_bicicleta_electrica_tarifa_correcta() {
        Rental rental = new Rental("BIC-003", "Pedro", BASE, 120);
        rental.finish(BASE.plusHours(1), BikeType.ELECTRICA.getHourlyRate());

        assertCost(new BigDecimal("7500"), rental.getTotalCost());
        assertFalse(rental.isHasPenalty());
    }

    // ── RN-03: multa por retraso ───────────────────────────────────────────

    @Test
    void multa_montana_3h20min_estimado_2h() {
        // MONTANA, 120min estimados, devuelta 3h20min después (200min)
        // real=200min → ceil=4h → base=4×5000=20000
        // delay desde min 120 hasta min 200 = 80min → ceil=2h → multa=2×2500=5000 → total=25000
        Rental rental = new Rental("BIC-002", "Luis", BASE, 120);
        rental.finish(BASE.plusMinutes(200), BikeType.MONTANA.getHourlyRate());

        assertCost(new BigDecimal("25000"), rental.getTotalCost());
        assertTrue(rental.isHasPenalty());
    }

    @Test
    void sin_multa_devolucion_exactamente_en_tiempo() {
        Rental rental = new Rental("BIC-001", "Ana", BASE, 120);
        rental.finish(BASE.plusHours(2), BikeType.URBANA.getHourlyRate());

        assertFalse(rental.isHasPenalty());
    }

    @Test
    void multa_retraso_minimo_1_minuto_cobra_1_hora() {
        // 1 min de retraso → ceil(1/60.0)=1h de multa
        Rental rental = new Rental("BIC-001", "Ana", BASE, 120);
        rental.finish(BASE.plusHours(2).plusMinutes(1), BikeType.URBANA.getHourlyRate());

        // base: ceil(121/60)=3h → 3×3500=10500
        // multa: ceil(1/60)=1h → 1×1750=1750 → total=12250
        assertCost(new BigDecimal("12250"), rental.getTotalCost());
        assertTrue(rental.isHasPenalty());
    }

    // ── RN-04: estado de bicicleta ─────────────────────────────────────────

    @Test
    void marcar_como_alquilada_bicicleta_disponible_exitoso() {
        Bike bike = new Bike("BIC-001", BikeType.URBANA, BikeStatus.DISPONIBLE);
        bike.markAsRented();
        assertEquals(BikeStatus.ALQUILADA, bike.getStatus());
    }

    @Test
    void marcar_como_alquilada_bicicleta_ya_alquilada_lanza_exception() {
        Bike bike = new Bike("BIC-001", BikeType.URBANA, BikeStatus.ALQUILADA);
        assertThrows(IllegalStateException.class, bike::markAsRented);
    }

    @Test
    void marcar_como_alquilada_bicicleta_en_mantenimiento_lanza_exception() {
        Bike bike = new Bike("BIC-004", BikeType.MONTANA, BikeStatus.EN_MANTENIMIENTO);
        assertThrows(IllegalStateException.class, bike::markAsRented);
    }

    // ── RN-05: prevención de doble finalización ────────────────────────────

    @Test
    void no_puede_finalizar_alquiler_ya_terminado() {
        Rental rental = new Rental("BIC-001", "Ana", BASE, 120);
        rental.finish(BASE.plusHours(2), BikeType.URBANA.getHourlyRate());

        assertThrows(IllegalStateException.class,
            () -> rental.finish(BASE.plusHours(3), BikeType.URBANA.getHourlyRate()));
    }
}

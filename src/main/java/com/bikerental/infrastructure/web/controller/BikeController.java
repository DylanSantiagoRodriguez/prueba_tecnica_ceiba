package com.bikerental.infrastructure.web.controller;

import com.bikerental.application.dto.request.RegisterBikeRequest;
import com.bikerental.application.dto.response.BikeResponse;
import com.bikerental.application.dto.response.RentalResponse;
import com.bikerental.domain.model.BikeType;
import com.bikerental.domain.port.in.GetAvailableBikesUseCase;
import com.bikerental.domain.port.in.GetRentalHistoryUseCase;
import com.bikerental.domain.port.in.RegisterBikeUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    private final RegisterBikeUseCase registerBikeUseCase;
    private final GetAvailableBikesUseCase getAvailableBikesUseCase;
    private final GetRentalHistoryUseCase getRentalHistoryUseCase;

    public BikeController(RegisterBikeUseCase registerBikeUseCase,
                          GetAvailableBikesUseCase getAvailableBikesUseCase,
                          GetRentalHistoryUseCase getRentalHistoryUseCase) {
        this.registerBikeUseCase = registerBikeUseCase;
        this.getAvailableBikesUseCase = getAvailableBikesUseCase;
        this.getRentalHistoryUseCase = getRentalHistoryUseCase;
    }

    @PostMapping
    public ResponseEntity<BikeResponse> register(@Valid @RequestBody RegisterBikeRequest request) {
        return ResponseEntity.status(201).body(registerBikeUseCase.register(request));
    }

    @GetMapping("/available")
    public ResponseEntity<List<BikeResponse>> getAvailable(
            @RequestParam(required = false) BikeType type) {
        return ResponseEntity.ok(getAvailableBikesUseCase.getAvailableBikes(type));
    }

    @GetMapping("/{code}/history")
    public ResponseEntity<List<RentalResponse>> getHistory(@PathVariable String code) {
        return ResponseEntity.ok(getRentalHistoryUseCase.getRentalHistory(code));
    }
}

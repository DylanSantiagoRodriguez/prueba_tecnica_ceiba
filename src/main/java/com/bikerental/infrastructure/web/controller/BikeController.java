package com.bikerental.infrastructure.web.controller;

import com.bikerental.application.dto.request.RegisterBikeRequest;
import com.bikerental.application.dto.response.BikeResponse;
import com.bikerental.application.dto.response.RentalResponse;
import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import com.bikerental.domain.port.in.DeleteBikeUseCase;
import com.bikerental.domain.port.in.GetAllBikesUseCase;
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
    private final DeleteBikeUseCase deleteBikeUseCase;
    private final GetAllBikesUseCase getAllBikesUseCase;
    private final GetAvailableBikesUseCase getAvailableBikesUseCase;
    private final GetRentalHistoryUseCase getRentalHistoryUseCase;

    public BikeController(RegisterBikeUseCase registerBikeUseCase,
                          DeleteBikeUseCase deleteBikeUseCase,
                          GetAllBikesUseCase getAllBikesUseCase,
                          GetAvailableBikesUseCase getAvailableBikesUseCase,
                          GetRentalHistoryUseCase getRentalHistoryUseCase) {
        this.registerBikeUseCase = registerBikeUseCase;
        this.deleteBikeUseCase = deleteBikeUseCase;
        this.getAllBikesUseCase = getAllBikesUseCase;
        this.getAvailableBikesUseCase = getAvailableBikesUseCase;
        this.getRentalHistoryUseCase = getRentalHistoryUseCase;
    }

    @GetMapping("")
    public ResponseEntity<List<BikeResponse>> getAll(
            @RequestParam(required = false) BikeStatus status,
            @RequestParam(required = false) BikeType type) {
        return ResponseEntity.ok(getAllBikesUseCase.getAllBikes(status, type));
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

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        deleteBikeUseCase.deleteBike(code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{code}/history")
    public ResponseEntity<List<RentalResponse>> getHistory(@PathVariable String code) {
        return ResponseEntity.ok(getRentalHistoryUseCase.getRentalHistory(code));
    }
}

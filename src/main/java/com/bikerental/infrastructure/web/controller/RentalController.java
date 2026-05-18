package com.bikerental.infrastructure.web.controller;

import com.bikerental.application.dto.request.FinishRentalRequest;
import com.bikerental.application.dto.request.StartRentalRequest;
import com.bikerental.application.dto.response.RentalResponse;
import com.bikerental.domain.port.in.FinishRentalUseCase;
import com.bikerental.domain.port.in.StartRentalUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final StartRentalUseCase startRentalUseCase;
    private final FinishRentalUseCase finishRentalUseCase;

    public RentalController(StartRentalUseCase startRentalUseCase,
                            FinishRentalUseCase finishRentalUseCase) {
        this.startRentalUseCase = startRentalUseCase;
        this.finishRentalUseCase = finishRentalUseCase;
    }

    @PostMapping
    public ResponseEntity<RentalResponse> startRental(@Valid @RequestBody StartRentalRequest request) {
        return ResponseEntity.status(201).body(startRentalUseCase.startRental(request));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<RentalResponse> finishRental(
            @PathVariable Long id,
            @Valid @RequestBody FinishRentalRequest request) {
        return ResponseEntity.ok(finishRentalUseCase.finishRental(id, request));
    }
}

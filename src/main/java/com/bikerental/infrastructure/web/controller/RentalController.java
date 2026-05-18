package com.bikerental.infrastructure.web.controller;

import com.bikerental.application.dto.request.StartRentalRequest;
import com.bikerental.application.dto.response.RentalResponse;
import com.bikerental.domain.port.in.FinishRentalUseCase;
import com.bikerental.domain.port.in.GetRentalsUseCase;
import com.bikerental.domain.port.in.StartRentalUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final StartRentalUseCase startRentalUseCase;
    private final FinishRentalUseCase finishRentalUseCase;
    private final GetRentalsUseCase getRentalsUseCase;

    public RentalController(StartRentalUseCase startRentalUseCase,
                            FinishRentalUseCase finishRentalUseCase,
                            GetRentalsUseCase getRentalsUseCase) {
        this.startRentalUseCase = startRentalUseCase;
        this.finishRentalUseCase = finishRentalUseCase;
        this.getRentalsUseCase = getRentalsUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RentalResponse>> getRentals(
            @RequestParam(required = false) Boolean finished) {
        return ResponseEntity.ok(getRentalsUseCase.getRentals(finished));
    }

    @PostMapping
    public ResponseEntity<RentalResponse> startRental(@Valid @RequestBody StartRentalRequest request) {
        return ResponseEntity.status(201).body(startRentalUseCase.startRental(request));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<RentalResponse> finishRental(@PathVariable Long id) {
        return ResponseEntity.ok(finishRentalUseCase.finishRental(id));
    }
}

package com.bikerental.domain.port.in;

import com.bikerental.application.dto.response.RentalResponse;

public interface FinishRentalUseCase {
    RentalResponse finishRental(Long rentalId);
}

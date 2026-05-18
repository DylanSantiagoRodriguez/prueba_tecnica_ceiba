package com.bikerental.domain.port.in;

import com.bikerental.application.dto.request.StartRentalRequest;
import com.bikerental.application.dto.response.RentalResponse;

public interface StartRentalUseCase {
    RentalResponse startRental(StartRentalRequest request);
}

package com.bikerental.domain.port.in;

import com.bikerental.application.dto.response.RentalResponse;

import java.util.List;

public interface GetRentalsUseCase {
    List<RentalResponse> getRentals(Boolean finished);
}

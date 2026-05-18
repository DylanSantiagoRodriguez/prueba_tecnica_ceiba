package com.bikerental.domain.port.in;

import com.bikerental.application.dto.request.RegisterBikeRequest;
import com.bikerental.application.dto.response.BikeResponse;

public interface RegisterBikeUseCase {
    BikeResponse register(RegisterBikeRequest request);
}

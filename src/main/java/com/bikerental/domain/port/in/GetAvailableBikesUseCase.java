package com.bikerental.domain.port.in;

import com.bikerental.application.dto.response.BikeResponse;
import com.bikerental.domain.model.BikeType;

import java.util.List;

public interface GetAvailableBikesUseCase {
    List<BikeResponse> getAvailableBikes(BikeType type);
}

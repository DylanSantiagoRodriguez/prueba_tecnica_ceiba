package com.bikerental.domain.port.in;

import com.bikerental.application.dto.response.BikeResponse;
import com.bikerental.domain.model.BikeStatus;

import java.util.List;

public interface GetAllBikesUseCase {
    List<BikeResponse> getAllBikes(BikeStatus status);
}

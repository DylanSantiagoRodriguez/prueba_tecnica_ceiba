package com.bikerental.domain.exception;

import com.bikerental.domain.model.BikeStatus;

public class BikeNotAvailableException extends RuntimeException {
    public BikeNotAvailableException(String code, BikeStatus currentStatus) {
        super("La bicicleta " + code + " no está disponible. Estado actual: " + currentStatus);
    }
}

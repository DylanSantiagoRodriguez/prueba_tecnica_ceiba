package com.bikerental.domain.exception;

public class RentalAlreadyFinishedException extends RuntimeException {
    public RentalAlreadyFinishedException(Long id) {
        super("El alquiler " + id + " ya fue finalizado");
    }
}

package com.bikerental.domain.exception;

public class RentalNotFoundException extends RuntimeException {
    public RentalNotFoundException(Long id) {
        super("Alquiler con id " + id + " no encontrado");
    }
}

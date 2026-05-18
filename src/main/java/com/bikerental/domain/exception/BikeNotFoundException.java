package com.bikerental.domain.exception;

public class BikeNotFoundException extends RuntimeException {
    public BikeNotFoundException(String code) {
        super("Bicicleta con código " + code + " no encontrada");
    }
}

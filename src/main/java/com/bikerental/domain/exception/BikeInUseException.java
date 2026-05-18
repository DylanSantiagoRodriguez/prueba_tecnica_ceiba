package com.bikerental.domain.exception;

public class BikeInUseException extends RuntimeException {
    public BikeInUseException(String code) {
        super("No se puede eliminar la bicicleta " + code + " porque tiene un alquiler activo");
    }
}

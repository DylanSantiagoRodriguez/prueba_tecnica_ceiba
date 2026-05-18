package com.bikerental.domain.model;

public class Bike {

    private String code;
    private BikeType type;
    private BikeStatus status;

    public Bike(String code, BikeType type, BikeStatus status) {
        this.code = code;
        this.type = type;
        this.status = status;
    }

    public boolean isAvailable() {
        return status == BikeStatus.DISPONIBLE;
    }

    public void markAsRented() {
        if (!isAvailable()) {
            throw new IllegalStateException(
                "La bicicleta " + code + " no está disponible. Estado actual: " + status);
        }
        this.status = BikeStatus.ALQUILADA;
    }

    public void markAsAvailable() {
        this.status = BikeStatus.DISPONIBLE;
    }

    public String getCode() { return code; }
    public BikeType getType() { return type; }
    public BikeStatus getStatus() { return status; }
}

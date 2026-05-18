package com.bikerental.domain.model;

import java.math.BigDecimal;

public enum BikeType {
    URBANA(new BigDecimal("3500")),
    MONTANA(new BigDecimal("5000")),
    ELECTRICA(new BigDecimal("7500"));

    private final BigDecimal hourlyRate;

    BikeType(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }
}

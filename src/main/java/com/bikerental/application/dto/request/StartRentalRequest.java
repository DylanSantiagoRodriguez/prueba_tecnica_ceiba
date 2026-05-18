package com.bikerental.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class StartRentalRequest {

    @NotBlank
    private String bikeCode;

    @NotBlank
    private String customerName;

    @Positive
    private int estimatedHours;

    public String getBikeCode() { return bikeCode; }
    public void setBikeCode(String bikeCode) { this.bikeCode = bikeCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(int estimatedHours) { this.estimatedHours = estimatedHours; }
}

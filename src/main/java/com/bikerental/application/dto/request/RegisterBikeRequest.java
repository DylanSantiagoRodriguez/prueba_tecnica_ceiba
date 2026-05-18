package com.bikerental.application.dto.request;

import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterBikeRequest {

    @NotBlank
    private String code;

    @NotNull
    private BikeType type;

    @NotNull
    private BikeStatus status;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public BikeType getType() { return type; }
    public void setType(BikeType type) { this.type = type; }

    public BikeStatus getStatus() { return status; }
    public void setStatus(BikeStatus status) { this.status = status; }
}

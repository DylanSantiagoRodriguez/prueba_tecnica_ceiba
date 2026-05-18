package com.bikerental.application.dto.response;

import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;

public class BikeResponse {

    private String code;
    private BikeType type;
    private BikeStatus status;

    public BikeResponse(String code, BikeType type, BikeStatus status) {
        this.code = code;
        this.type = type;
        this.status = status;
    }

    public String getCode() { return code; }
    public BikeType getType() { return type; }
    public BikeStatus getStatus() { return status; }
}

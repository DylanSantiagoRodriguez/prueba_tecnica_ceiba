package com.bikerental.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class FinishRentalRequest {

    @NotNull
    private LocalDateTime returnTime;

    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }
}

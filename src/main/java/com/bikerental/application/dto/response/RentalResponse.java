package com.bikerental.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RentalResponse {

    private Long id;
    private String bikeCode;
    private String customerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer realDurationHours;
    private BigDecimal totalCost;
    private boolean hasPenalty;
    private boolean finished;

    public RentalResponse(Long id, String bikeCode, String customerName,
                          LocalDateTime startTime, LocalDateTime endTime,
                          Integer realDurationHours, BigDecimal totalCost,
                          boolean hasPenalty, boolean finished) {
        this.id = id;
        this.bikeCode = bikeCode;
        this.customerName = customerName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.realDurationHours = realDurationHours;
        this.totalCost = totalCost;
        this.hasPenalty = hasPenalty;
        this.finished = finished;
    }

    public Long getId() { return id; }
    public String getBikeCode() { return bikeCode; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Integer getRealDurationHours() { return realDurationHours; }
    public BigDecimal getTotalCost() { return totalCost; }
    public boolean isHasPenalty() { return hasPenalty; }
    public boolean isFinished() { return finished; }
}

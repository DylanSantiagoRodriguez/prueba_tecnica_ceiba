package com.bikerental.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class Rental {

    private Long id;
    private String bikeCode;
    private String customerName;
    private LocalDateTime startTime;
    private int estimatedMinutes;
    private LocalDateTime endTime;
    private BigDecimal totalCost;
    private boolean hasPenalty;

    public Rental(String bikeCode, String customerName, LocalDateTime startTime, int estimatedMinutes) {
        this.bikeCode = bikeCode;
        this.customerName = customerName;
        this.startTime = startTime;
        this.estimatedMinutes = estimatedMinutes;
        this.hasPenalty = false;
    }

    public Rental(Long id, String bikeCode, String customerName, LocalDateTime startTime,
                  int estimatedMinutes, LocalDateTime endTime, BigDecimal totalCost, boolean hasPenalty) {
        this.id = id;
        this.bikeCode = bikeCode;
        this.customerName = customerName;
        this.startTime = startTime;
        this.estimatedMinutes = estimatedMinutes;
        this.endTime = endTime;
        this.totalCost = totalCost;
        this.hasPenalty = hasPenalty;
    }

    public boolean isFinished() {
        return endTime != null;
    }

    /**
     * RN-02: costo base redondeado al alza por hora.
     * RN-03: multa del 50% por hora de retraso, calculada desde la hora estimada de devolución.
     */
    public void finish(LocalDateTime returnTime, BigDecimal hourlyRate) {
        if (isFinished()) {
            throw new IllegalStateException("El alquiler " + id + " ya fue finalizado");
        }
        this.endTime = returnTime;

        long realMinutes = Duration.between(startTime, returnTime).toMinutes();
        long realHours = Math.max(1, (long) Math.ceil(realMinutes / 60.0));
        BigDecimal baseCost = hourlyRate.multiply(BigDecimal.valueOf(realHours));

        LocalDateTime estimatedReturnTime = startTime.plusMinutes(estimatedMinutes);
        BigDecimal penaltyCost = BigDecimal.ZERO;

        if (returnTime.isAfter(estimatedReturnTime)) {
            long delayMinutes = Duration.between(estimatedReturnTime, returnTime).toMinutes();
            long delayHours = (long) Math.ceil(delayMinutes / 60.0);
            BigDecimal penaltyRate = hourlyRate.multiply(new BigDecimal("0.5"));
            penaltyCost = penaltyRate.multiply(BigDecimal.valueOf(delayHours));
            this.hasPenalty = true;
        }

        this.totalCost = baseCost.add(penaltyCost);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBikeCode() { return bikeCode; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public LocalDateTime getEndTime() { return endTime; }
    public BigDecimal getTotalCost() { return totalCost; }
    public boolean isHasPenalty() { return hasPenalty; }
}

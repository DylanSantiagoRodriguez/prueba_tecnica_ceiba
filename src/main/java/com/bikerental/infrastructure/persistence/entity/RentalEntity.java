package com.bikerental.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
public class RentalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bikeCode;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private int estimatedHours;

    @Column(nullable = true)
    private LocalDateTime endTime;

    @Column(nullable = true)
    private BigDecimal totalCost;

    @Column(nullable = false)
    private boolean hasPenalty;

    public RentalEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBikeCode() { return bikeCode; }
    public void setBikeCode(String bikeCode) { this.bikeCode = bikeCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public int getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(int estimatedHours) { this.estimatedHours = estimatedHours; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public boolean isHasPenalty() { return hasPenalty; }
    public void setHasPenalty(boolean hasPenalty) { this.hasPenalty = hasPenalty; }
}

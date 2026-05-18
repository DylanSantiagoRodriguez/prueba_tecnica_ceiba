package com.bikerental.infrastructure.persistence.mapper;

import com.bikerental.domain.model.Rental;
import com.bikerental.infrastructure.persistence.entity.RentalEntity;

public class RentalMapper {

    private RentalMapper() {}

    public static Rental toDomain(RentalEntity entity) {
        return new Rental(
                entity.getId(),
                entity.getBikeCode(),
                entity.getCustomerName(),
                entity.getStartTime(),
                entity.getEstimatedMinutes(),
                entity.getEndTime(),
                entity.getTotalCost(),
                entity.isHasPenalty()
        );
    }

    public static RentalEntity toEntity(Rental rental) {
        RentalEntity entity = new RentalEntity();
        entity.setBikeCode(rental.getBikeCode());
        entity.setCustomerName(rental.getCustomerName());
        entity.setStartTime(rental.getStartTime());
        entity.setEstimatedMinutes(rental.getEstimatedMinutes());
        entity.setEndTime(rental.getEndTime());
        entity.setTotalCost(rental.getTotalCost());
        entity.setHasPenalty(rental.isHasPenalty());
        return entity;
    }
}

package com.bikerental.infrastructure.persistence.mapper;

import com.bikerental.domain.model.Bike;
import com.bikerental.infrastructure.persistence.entity.BikeEntity;

public class BikeMapper {

    private BikeMapper() {}

    public static Bike toDomain(BikeEntity entity) {
        return new Bike(entity.getCode(), entity.getType(), entity.getStatus());
    }

    public static BikeEntity toEntity(Bike bike) {
        return new BikeEntity(bike.getCode(), bike.getType(), bike.getStatus());
    }
}

package com.bikerental.domain.port.out;

import com.bikerental.domain.model.Bike;
import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;

import java.util.List;
import java.util.Optional;

public interface BikeRepository {
    Optional<Bike> findByCode(String code);
    Bike save(Bike bike);
    List<Bike> findAll();
    List<Bike> findByStatus(BikeStatus status);
    List<Bike> findByType(BikeType type);
    List<Bike> findByStatusAndType(BikeStatus status, BikeType type);
    void deleteByCode(String code);
}

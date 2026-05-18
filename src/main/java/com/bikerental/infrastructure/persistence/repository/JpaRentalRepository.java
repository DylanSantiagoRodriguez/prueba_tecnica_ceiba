package com.bikerental.infrastructure.persistence.repository;

import com.bikerental.infrastructure.persistence.entity.RentalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRentalRepository extends JpaRepository<RentalEntity, Long> {
    List<RentalEntity> findByBikeCode(String bikeCode);
}

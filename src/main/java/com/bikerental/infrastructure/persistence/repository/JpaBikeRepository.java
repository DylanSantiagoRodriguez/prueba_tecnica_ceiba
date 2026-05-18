package com.bikerental.infrastructure.persistence.repository;

import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import com.bikerental.infrastructure.persistence.entity.BikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaBikeRepository extends JpaRepository<BikeEntity, Long> {
    Optional<BikeEntity> findByCode(String code);
    List<BikeEntity> findByStatus(BikeStatus status);
    List<BikeEntity> findByStatusAndType(BikeStatus status, BikeType type);
}

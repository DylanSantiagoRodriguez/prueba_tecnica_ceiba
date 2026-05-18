package com.bikerental.infrastructure.persistence.adapter;

import com.bikerental.domain.model.Bike;
import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import com.bikerental.domain.port.out.BikeRepository;
import com.bikerental.infrastructure.persistence.entity.BikeEntity;
import com.bikerental.infrastructure.persistence.mapper.BikeMapper;
import com.bikerental.infrastructure.persistence.repository.JpaBikeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BikeRepositoryAdapter implements BikeRepository {

    private final JpaBikeRepository jpaRepository;

    public BikeRepositoryAdapter(JpaBikeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Bike> findByCode(String code) {
        return jpaRepository.findByCode(code).map(BikeMapper::toDomain);
    }

    @Override
    public Bike save(Bike bike) {
        Optional<BikeEntity> existing = jpaRepository.findByCode(bike.getCode());
        BikeEntity entity = existing.orElseGet(() -> BikeMapper.toEntity(bike));
        entity.setStatus(bike.getStatus());
        entity.setType(bike.getType());
        return BikeMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Bike> findByStatus(BikeStatus status) {
        return jpaRepository.findByStatus(status)
                .stream().map(BikeMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Bike> findByStatusAndType(BikeStatus status, BikeType type) {
        return jpaRepository.findByStatusAndType(status, type)
                .stream().map(BikeMapper::toDomain).collect(Collectors.toList());
    }
}

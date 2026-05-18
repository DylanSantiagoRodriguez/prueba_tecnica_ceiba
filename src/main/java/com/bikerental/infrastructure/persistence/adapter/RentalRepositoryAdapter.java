package com.bikerental.infrastructure.persistence.adapter;

import com.bikerental.domain.model.Rental;
import com.bikerental.domain.port.out.RentalRepository;
import com.bikerental.infrastructure.persistence.entity.RentalEntity;
import com.bikerental.infrastructure.persistence.mapper.RentalMapper;
import com.bikerental.infrastructure.persistence.repository.JpaRentalRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RentalRepositoryAdapter implements RentalRepository {

    private final JpaRentalRepository jpaRepository;

    public RentalRepositoryAdapter(JpaRentalRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Rental save(Rental rental) {
        RentalEntity entity;
        if (rental.getId() != null) {
            entity = jpaRepository.findById(rental.getId())
                    .orElseGet(() -> RentalMapper.toEntity(rental));
            entity.setEndTime(rental.getEndTime());
            entity.setTotalCost(rental.getTotalCost());
            entity.setHasPenalty(rental.isHasPenalty());
        } else {
            entity = RentalMapper.toEntity(rental);
        }
        return RentalMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Rental> findById(Long id) {
        return jpaRepository.findById(id).map(RentalMapper::toDomain);
    }

    @Override
    public List<Rental> findByBikeCode(String bikeCode) {
        return jpaRepository.findByBikeCode(bikeCode)
                .stream().map(RentalMapper::toDomain).collect(Collectors.toList());
    }
}

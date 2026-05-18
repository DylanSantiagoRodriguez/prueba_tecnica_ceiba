package com.bikerental.domain.port.out;

import com.bikerental.domain.model.Rental;

import java.util.List;
import java.util.Optional;

public interface RentalRepository {
    Rental save(Rental rental);
    Optional<Rental> findById(Long id);
    List<Rental> findByBikeCode(String bikeCode);
}

package com.bikerental.infrastructure.config;

import com.bikerental.domain.model.Bike;
import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import com.bikerental.domain.port.out.BikeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BikeRepository bikeRepository;

    public DataInitializer(BikeRepository bikeRepository) {
        this.bikeRepository = bikeRepository;
    }

    @Override
    public void run(String... args) {
        List<Bike> referenceBikes = List.of(
                new Bike("BIC-001", BikeType.URBANA, BikeStatus.DISPONIBLE),
                new Bike("BIC-002", BikeType.MONTANA, BikeStatus.DISPONIBLE),
                new Bike("BIC-003", BikeType.ELECTRICA, BikeStatus.DISPONIBLE),
                new Bike("BIC-004", BikeType.MONTANA, BikeStatus.EN_MANTENIMIENTO),
                new Bike("BIC-005", BikeType.URBANA, BikeStatus.DISPONIBLE)
        );

        for (Bike bike : referenceBikes) {
            if (bikeRepository.findByCode(bike.getCode()).isEmpty()) {
                bikeRepository.save(bike);
            }
        }
    }
}

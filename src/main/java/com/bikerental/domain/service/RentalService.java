package com.bikerental.domain.service;

import com.bikerental.application.dto.request.RegisterBikeRequest;
import com.bikerental.application.dto.request.StartRentalRequest;
import com.bikerental.application.dto.response.BikeResponse;
import com.bikerental.application.dto.response.RentalResponse;
import com.bikerental.domain.exception.BikeNotAvailableException;
import com.bikerental.domain.exception.BikeNotFoundException;
import com.bikerental.domain.exception.RentalAlreadyFinishedException;
import com.bikerental.domain.exception.RentalNotFoundException;
import com.bikerental.domain.model.Bike;
import com.bikerental.domain.model.Rental;
import com.bikerental.domain.port.in.*;
import com.bikerental.domain.port.out.BikeRepository;
import com.bikerental.domain.port.out.RentalRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService implements
        RegisterBikeUseCase,
        StartRentalUseCase,
        FinishRentalUseCase,
        GetAvailableBikesUseCase,
        GetAllBikesUseCase,
        GetRentalsUseCase,
        GetRentalHistoryUseCase {

    private final BikeRepository bikeRepository;
    private final RentalRepository rentalRepository;

    public RentalService(BikeRepository bikeRepository, RentalRepository rentalRepository) {
        this.bikeRepository = bikeRepository;
        this.rentalRepository = rentalRepository;
    }

    @Override
    public BikeResponse register(RegisterBikeRequest request) {
        Bike bike = new Bike(request.getCode(), request.getType(), request.getStatus());
        Bike saved = bikeRepository.save(bike);
        return toBikeResponse(saved);
    }

    @Override
    public RentalResponse startRental(StartRentalRequest request) {
        Bike bike = bikeRepository.findByCode(request.getBikeCode())
                .orElseThrow(() -> new BikeNotFoundException(request.getBikeCode()));

        if (!bike.isAvailable()) {
            throw new BikeNotAvailableException(bike.getCode(), bike.getStatus());
        }

        bike.markAsRented();
        bikeRepository.save(bike);

        Rental rental = new Rental(
                bike.getCode(),
                request.getCustomerName(),
                LocalDateTime.now(),
                request.getEstimatedMinutes()
        );
        Rental saved = rentalRepository.save(rental);
        return toRentalResponse(saved);
    }

    @Override
    public RentalResponse finishRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));

        if (rental.isFinished()) {
            throw new RentalAlreadyFinishedException(rentalId);
        }

        Bike bike = bikeRepository.findByCode(rental.getBikeCode())
                .orElseThrow(() -> new BikeNotFoundException(rental.getBikeCode()));

        rental.finish(LocalDateTime.now(), bike.getType().getHourlyRate());
        bike.markAsAvailable();

        rentalRepository.save(rental);
        bikeRepository.save(bike);

        return toRentalResponse(rental);
    }

    @Override
    public List<BikeResponse> getAvailableBikes(com.bikerental.domain.model.BikeType type) {
        List<Bike> bikes = type != null
                ? bikeRepository.findByStatusAndType(com.bikerental.domain.model.BikeStatus.DISPONIBLE, type)
                : bikeRepository.findByStatus(com.bikerental.domain.model.BikeStatus.DISPONIBLE);
        return bikes.stream().map(this::toBikeResponse).collect(Collectors.toList());
    }

    @Override
    public List<BikeResponse> getAllBikes(com.bikerental.domain.model.BikeStatus status) {
        List<Bike> bikes = status != null
                ? bikeRepository.findByStatus(status)
                : bikeRepository.findAll();
        return bikes.stream().map(this::toBikeResponse).collect(Collectors.toList());
    }

    @Override
    public List<RentalResponse> getRentals(Boolean finished) {
        List<Rental> rentals = finished == null
                ? rentalRepository.findAll()
                : rentalRepository.findByFinished(finished);
        return rentals.stream().map(this::toRentalResponse).collect(Collectors.toList());
    }

    @Override
    public List<RentalResponse> getRentalHistory(String bikeCode) {
        return rentalRepository.findByBikeCode(bikeCode)
                .stream()
                .map(this::toRentalResponse)
                .collect(Collectors.toList());
    }

    private BikeResponse toBikeResponse(Bike bike) {
        return new BikeResponse(bike.getCode(), bike.getType(), bike.getStatus());
    }

    private RentalResponse toRentalResponse(Rental rental) {
        Integer durationMinutes = null;
        if (rental.isFinished()) {
            durationMinutes = (int) Duration.between(rental.getStartTime(), rental.getEndTime()).toMinutes();
        }
        return new RentalResponse(
                rental.getId(),
                rental.getBikeCode(),
                rental.getCustomerName(),
                rental.getStartTime(),
                rental.getEndTime(),
                durationMinutes,
                rental.getTotalCost(),
                rental.isHasPenalty(),
                rental.isFinished()
        );
    }
}

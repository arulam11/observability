package com.trypsync.testproject.ride.service;

import com.trypsync.testproject.outbox.OutboxWriter;
import com.trypsync.testproject.ride.domain.Ride;
import com.trypsync.testproject.ride.domain.RideRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RideService {
  private final RideRepository rideRepo;
  private final OutboxWriter outbox;

  public RideService(RideRepository rideRepo, OutboxWriter outbox) {
    this.rideRepo = rideRepo;
    this.outbox = outbox;
  }
  
  @PostConstruct
  void init() { System.out.println("RideService bean created"); }

  @Transactional
  public Ride createRide(Ride ride) {
    var saved = rideRepo.save(ride);

    outbox.append(
      "ride",
      String.valueOf(saved.getId()),
      "ride.created",
      Map.of(
        "id", saved.getId(),
        "origin", saved.getOrigin(),
        "destination", saved.getDestination(),
        "departureTime", saved.getDepartureTime(),
        "driverName", saved.getDriverName()
      )
    );

    return saved;
  }
}

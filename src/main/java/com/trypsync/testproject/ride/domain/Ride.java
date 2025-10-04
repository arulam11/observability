package com.trypsync.testproject.ride.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
public class Ride {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String origin;
  private String destination;
  private Instant departureTime;
  private Integer seatsAvailable;
  private Integer priceCents;
  private String driverName;
  private Instant createdAt = Instant.now();

}
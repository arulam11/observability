package com.trypsync.testproject.ride.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.trypsync.testproject.ride.domain.Ride;
import com.trypsync.testproject.ride.domain.RideRepository;
import com.trypsync.testproject.ride.service.RideService;

@RestController
@RequestMapping("/api/rides")
public class RideController {
  private final RideRepository repo;
  private final RideService service;
  public RideController(RideRepository repo, RideService service) {
    this.repo = repo;
    this.service = service;
  }

  @GetMapping public List<Ride> list(){ return repo.findAll(); }

  // Get one ride
  @GetMapping("/{id}")
  public ResponseEntity<Ride> get(@PathVariable Long id) {
    return repo.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Map<String,Object>> create(@RequestBody Ride ride){
    Map<String,Object> body = new HashMap<>();
    try {
      System.out.println("Creating ride: " + ride);
      var saved = service.createRide(ride);
      System.out.println("Saved  ride: " + saved);

      body.put("success", true);
      body.put("id", saved.getId());
      body.put("message", "Ride created");
      return ResponseEntity.created(URI.create("/api/rides/" + saved.getId())).body(body);
    } catch (Exception e) {
      // log the exception and return a friendly error message
      e.printStackTrace();
      body.put("success", false);
      body.put("message", "Failed to create ride: " + e.getMessage());
      return ResponseEntity.status(500).body(body);
    }
  }
  
}

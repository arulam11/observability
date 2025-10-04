package com.trypsync.testproject.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OutboxWriter {
  private final JdbcTemplate jdbc;
  private final ObjectMapper om;

  public OutboxWriter(JdbcTemplate jdbc, ObjectMapper om) {
    this.jdbc = jdbc;
    this.om = om;
  }

  @PostConstruct
  void init() { System.out.println("OutboxWriter bean created"); }

  public void append(String aggregateType, String aggregateId, String type, Map<String, ?> payload) {
    try {
      String json = om.writeValueAsString(payload);
      jdbc.update("""
          INSERT INTO outbox_events
          (aggregate_type, aggregate_id, type, payload, status, attempts)
          VALUES (?, ?, ?, CAST(? AS jsonb), 'PENDING', 0)
          """,
          aggregateType, aggregateId, type, json
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to write outbox event", e);
    }
  }
}

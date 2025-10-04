package com.trypsync.testproject.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;
import java.util.Map;


@Component
public class OutboxPublisher {

  private final JdbcTemplate jdbc;
  private final KafkaTemplate<String, String> kafka;
  private final String topic;
  private final int batchSize;
  private final int backoffSeconds;
  private final int maxAttempts;
  private final MeterRegistry meter;  

  public OutboxPublisher(
      JdbcTemplate jdbc,
      KafkaTemplate<String, String> kafka,
      @Value("${app.outbox.topic:ride.created}") String topic,
      @Value("${app.outbox.batch-size:50}") int batchSize,
      @Value("${app.outbox.backoff-seconds:30}") int backoffSeconds,
      @Value("${app.outbox.max-attempts:0}") int maxAttempts,
      MeterRegistry meterRegistry) {
    this.jdbc = jdbc;
    this.kafka = kafka;
    this.topic = topic;
    this.batchSize = batchSize;
    this.backoffSeconds = backoffSeconds;
    this.maxAttempts = maxAttempts;
    this.meter = meterRegistry;    
  }

  @Scheduled(fixedDelayString = "${app.outbox.poll-ms:2000}")
  @Transactional
  public void publishBatch() {
    System.out.println("Publish batch started");
    // Lock a batch with SKIP LOCKED
    List<Map<String, Object>> rows = jdbc.queryForList("""
      SELECT id, aggregate_id, payload::text AS payload, attempts
      FROM outbox_events
      WHERE status = 'PENDING' AND available_at <= now()
      ORDER BY id
      FOR UPDATE SKIP LOCKED
      LIMIT ?
    """, batchSize);

    for (var r : rows) {
      Long id = ((Number) r.get("id")).longValue();
      String key = (String) r.get("aggregate_id");
      String payload = (String) r.get("payload");
      Integer attempts = ((Number) r.get("attempts")).intValue();

      try {
        kafka.send(topic, key, payload).get(); // wait to confirm success
        jdbc.update("""
            UPDATE outbox_events
               SET status='PUBLISHED', published_at=now()
             WHERE id = ?
            """, id);
        meter.counter("outbox.events.published").increment();
      } catch (Exception ex) {
        attempts++;
        // Terminate after N attempts if configured
        if (maxAttempts > 0 && attempts >= maxAttempts) {
            jdbc.update("""
                UPDATE outbox_events
                 SET status='FAILED', attempts = ?
                WHERE id = ?
               """, attempts, id);
           meter.counter("outbox.events.failed.permanent").increment();
          // (Optional) log error for alerting systems
          // log.error("Outbox event {} permanently failed after {} attempts", id, attempts, ex);
        } else {
           long backoff = Math.min(600L, (long) attempts * backoffSeconds); // cap to 10m
           jdbc.update("""
               UPDATE outbox_events
                  SET attempts = ?,
                      available_at = now() + make_interval(secs => ?),
                      status = 'PENDING'
                WHERE id = ?
               """, attempts, backoff, id);
           meter.counter("outbox.events.retry").increment();
       }            
      }
    }
  }
}

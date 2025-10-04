# Observability – Spring Boot + Postgres + Flyway + Kafka (Outbox) + Prometheus/Grafana

[![Build](https://github.com/arulam11/observability/actions/workflows/maven.yml/badge.svg)](https://github.com/arulam11/observability/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-21-1f425f.svg)
![Spring_Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F.svg)
![Kafka](https://img.shields.io/badge/Kafka-Dev-231F20.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

A minimal, production-leaning template:
- **Spring Boot 3** (Actuator, Micrometer)
- **Postgres** with **Flyway** migrations
- **Transactional Outbox** → publishes to **Kafka**
- **Prometheus** scrape via `/actuator/prometheus`
- **Grafana** dashboards (Kafka + app metrics)

---

## Architecture (short)

[Rides API] --(JPA TX)--> [Postgres]
--> [Outbox row]

[OutboxPublisher @Scheduled]
-> SELECT ... FOR UPDATE SKIP LOCKED
-> send to [Kafka topic: ride.created]
-> mark PUBLISHED / retry w/ backoff

[Micrometer]
-> outbox.events.* counters
-> kafka_producer_* metrics
-> /actuator/prometheus

swift
Copy code

---

## Quick start

```bash
# 0) (first time) clone
git clone https://github.com/xxx/observability.git
cd observability

# 1) Start infra (if using the combined docker-compose.yml)
docker compose up -d

# 2) Run the app (profiles can be grouped as 'dev = postgres,kafka')
setx SPRING_PROFILES_ACTIVE "postgres,kafka"
.\mvnw.cmd spring-boot:run

# 3) Create a ride (produces Kafka message via outbox)
curl -X POST http://localhost:8080/api/rides -H "Content-Type: application/json" ^
  -d "{\"origin\":\"Emory\",\"destination\":\"ATL\",\"departureTime\":\"2025-10-02T14:30:00Z\",\"seatsAvailable\":3,\"priceCents\":1500,\"driverName\":\"Akhil\"}"
Endpoints

App metrics: http://localhost:8080/actuator/prometheus

Prometheus: http://localhost:9090

Grafana: http://localhost:3000 (default: admin/admin)

Configuration
Create local profile files from the samples (kept out of Git):

bash
Copy code
copy src\main\resources\application-postgres.example.yml src\main\resources\application-postgres.yml
copy src\main\resources\application-kafka.example.yml    src\main\resources\application-kafka.yml
Environment overrides you’ll commonly use:

SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD

KAFKA_BOOTSTRAP (e.g., localhost:9092)

Observability (what you get)
Outbox metrics (Micrometer counters):

outbox_events_published_total

outbox_events_retry_total

outbox_events_failed_permanent_total

Kafka producer metrics (Micrometer KafkaClientMetrics):

kafka_producer_record_send_total

kafka_producer_record_error_total

kafka_producer_request_total, etc.

Suggested Grafana panels

rate(outbox_events_published_total[1m])

sum by (client_id) (rate(kafka_producer_record_send_total[1m]))

sum(rate(kafka_producer_record_error_total[1m]))

(Optional) Add kafka-exporter for broker/topic/lag and chart:

sum by (consumergroup) (kafka_consumergroup_lag)

Migrations
Flyway auto-runs on startup (see src/main/resources/db/migration).

Example: V1__init.sql (schema), V4__create_outbox_events.sql, etc.

Dev notes
Transactional Outbox keeps domain clean (no JPA entity for outbox): JPA write + JDBC insert in one TX.

FOR UPDATE SKIP LOCKED enables safe parallel publishers; add advisory lock if you need strict global order.

Retries capped with app.outbox.max-attempts; failed rows → FAILED (or DLQ table if enabled).

License
This project is licensed under the Apache-2.0 License.
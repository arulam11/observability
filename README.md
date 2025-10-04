# Observability — Spring Boot + Postgres + Flyway + Kafka (Outbox) + Prometheus/Grafana

A minimal, production-leaning template:
- **Spring Boot 3** (Actuator, Micrometer)
- **Postgres** with **Flyway** migrations
- **Transactional Outbox** → publishes to **Kafka**
- **Prometheus** scrape via `/actuator/prometheus`
- **Grafana** dashboards (Kafka + app metrics)
- Local dev via **Docker Compose**

## Architecture (short)
1. App writes business data (**rides**) and an **outbox event** in the same DB transaction.
2. A scheduled **OutboxPublisher** polls `outbox_events` and sends to Kafka.
3. Micrometer exposes:
   - `outbox.events.published|retry|failed.permanent`
   - Kafka producer metrics (`kafka_producer_*`)

## Quick start
```bash
# 1) Start infra (Kafka, Postgres, Prometheus, Grafana) if using compose
docker compose up -d

# 2) Run the app (profiles can be grouped as 'dev')
setx SPRING_PROFILES_ACTIVE "postgres,kafka"
.\mvnw.cmd spring-boot:run

# 3) Create a ride
curl -X POST http://localhost:8080/api/rides -H "Content-Type: application/json" ^
  -d "{\"origin\":\"Bridgewater\",\"destination\":\"NYC\",\"departureTime\":\"2025-10-02T14:30:00Z\",\"seatsAvailable\":3,\"priceCents\":1500,\"driverName\":\"Arularasu\"}"

Observability

Prometheus scrape: http://localhost:9090

Grafana: http://localhost:3000 (default creds admin/admin)

App metrics: http://localhost:8080/actuator/prometheus

Migrations

Flyway auto-runs on startup (see src/main/resources/db/migration).

License

Apache-2.0 (see LICENSE)
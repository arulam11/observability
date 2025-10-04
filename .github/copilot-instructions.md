<!-- Copilot instructions for the testproject repository -->
# Copilot / AI Agent guidance — testproject

This file contains concise, actionable guidance to help an AI coding agent be productive in this small Spring Boot example app.

High-level architecture
- This is a single-module Spring Boot application (Java 17) using Spring Web, Spring Data JPA and Testcontainers for tests. See `pom.xml` for dependencies and `src/main/java/com/trypsync/testproject` for sources.
- The app exposes a simple REST API under `/api/rides` implemented by `src/main/java/com/trypsync/testproject/ride/web/RideController.java` and persists `Ride` entities (`ride/domain/Ride.java`) via `RideRepository` (`ride/domain/RideRepository.java`).
- Tests start Spring Boot context and wire a Testcontainers Kafka container from `src/test/java/com/trypsync/testproject/TestcontainersConfiguration.java`.

Key developer workflows (commands)
- Build and run tests (Maven, Windows PowerShell):
  - mvn -q -DskipTests=false test
  - mvn -q package
  - To run the app locally: mvn -q spring-boot:run
- Tests rely on Testcontainers; Docker must be available for full integration tests. Unit-style tests that don't require Kafka still run with the embedded H2 DB configured in `src/main/resources/application.yml`.

Project-specific patterns and conventions
- Minimal layered layout: controller under `ride/web`, domain + repository under `ride/domain`. Follow this package separation for new features.
- Entities use JPA annotations and rely on H2 (Postgres compatibility mode) in `application.yml`. Schema is created/updated at runtime (`spring.jpa.hibernate.ddl-auto: update`). Don't assume production-safe migration here.
- Controllers return domain objects directly (no DTOs currently). When adding APIs, preserve simple patterns or introduce DTOs and map explicitly — update controller tests accordingly.
- Lombok is declared in `pom.xml` and may be used to reduce boilerplate. Current entity code may use Lombok annotations (e.g. `@Getter`, `@Setter`, `@NoArgsConstructor`).
  - Do NOT use `@Data` on JPA entities. Prefer `@Getter`/`@Setter` and `@NoArgsConstructor`. If defining equality, use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` and explicitly include stable fields.
  - Ensure IDEs enable annotation processing or install the Lombok plugin so generated methods are recognized.

Integration points and external dependencies
- Kafka: tests configure a `KafkaContainer` (`TestcontainersConfiguration.java`) using image `apache/kafka-native:latest`. Tests import this configuration via `@Import(TestcontainersConfiguration.class)`.
- Metrics/tracing: Micrometer OTLP and Prometheus registries are present as runtime dependencies in `pom.xml`. No runtime config in source files — only include if adding observability.

Behavioral hints for edits and PRs
- Keep controller logic thin — delegate data access to `RideRepository`.
- Tests in `src/test/java` use Spring Boot test slicing (full context). For lightweight unit tests prefer plain JUnit without starting the Spring context.
- When modifying entities, update database assumptions in `application.yml` only if needed for local dev. Prefer creating migrations (not yet present) when changing production-affecting schema.

Files worth reading for context
- `pom.xml` — dependency and plugin decisions (Java 17, Spring Boot 3.5.6)
- `src/main/resources/application.yml` — H2 datasource config and JPA defaults
- `src/main/java/com/trypsync/testproject/ride/web/RideController.java` — REST API patterns
- `src/main/java/com/trypsync/testproject/ride/domain/Ride.java` — entity shape and fields
- `src/test/java/com/trypsync/testproject/TestcontainersConfiguration.java` — how tests spin up Kafka

Editing rules for AI
- Only modify files relevant to the task; avoid wide refactors. Keep changes minimal and focused.
- Preserve package structure and follow Java naming conventions used in the repo.
- If adding dependencies, update `pom.xml` and ensure tests still run locally (Docker required for Testcontainers tests).

If something is unclear
- Ask for clarification about intended runtime environment (Docker available for Testcontainers?) or whether DTOs/validation should be introduced.

End of guidance.

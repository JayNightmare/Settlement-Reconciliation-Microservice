# Settlement Reconciliation Microservice

Production-ready Spring Boot 3 microservice that ingests trade and ledger transactions, performs settlement reconciliation, and exposes APIs for operations teams at a Tier-1 bank scale.

## Features
- Spring Batch ingestion pipeline (CSV today, extensible to S3/Azure Blob) for trades and ledger feeds
- Reconciliation engine with currency-aware tolerances, mismatch persistence, and Kafka event publishing
- REST APIs to trigger reconciliation, inspect and resolve mismatches, and orchestrate ingestion
- Scheduled nightly run and SLA-driven escalation emails for unresolved mismatches
- OAuth2/JWT protected endpoints with role-based access (`OPS_ANALYST`, `ADMIN`)
- Observability via Micrometer metrics, health checks, and structured logging conventions
- PostgreSQL persistence with Flyway migrations; Docker Compose stack for local dependencies
- CI-ready: Maven build, Spotless formatting, Testcontainers integration tests, GitHub Actions workflow

## Getting Started
1. **Prerequisites**: Java 24, Maven 3.9+, Docker, Docker Compose, local SMTP stub (optional).
2. **Bootstrap dependencies**: `docker compose up -d` (spins up PostgreSQL, Kafka, Schema Registry).
3. **Build & test**: `mvn clean verify` (integration tests require Docker for Testcontainers).
4. **Run the app**: `mvn spring-boot:run` or build the JAR and run via Docker: `docker build -t settlement-service . && docker run --rm -p 8080:8080 settlement-service`.
5. **Explore APIs**: Swagger UI at `http://localhost:8080/swagger-ui/index.html`, health checks at `/actuator/health`.

## Configuration
Key configuration lives in `src/main/resources/application.yaml` and supports environment overrides through variables.

| Property | Description |
| --- | --- |
| `reconciliation.tolerances` | Currency-specific amount tolerance before flagging a mismatch |
| `reconciliation.kafka.mismatch-topic` | Kafka topic for mismatch events |
| `reconciliation.escalation.sla` | Duration before unresolved mismatches are escalated (ISO-8601 duration) |
| `spring.kafka.bootstrap-servers` | Kafka bootstrap servers (default `localhost:9092`) |
| `spring.datasource.*` | PostgreSQL connection configuration |
| `spring.security.oauth2.resourceserver.jwt.*` | Configure issuer/public key for JWT validation |

Secrets (DB password, OAuth issuer, SMTP credentials) should be supplied via environment variables or secret managers (e.g., Vault, Azure Key Vault) before deployment.

## Architecture Highlights
- **Layers**: REST controllers → service layer → repositories/entities (Spring Data JPA)
- **Batch ingestion**: Configured in `SettlementBatchConfiguration`, using `IngestionSourceProvider` to abstract storage. Default implementation reads sample CSVs on the classpath for local demos.
- **Reconciliation**: `DefaultReconciliationService` compares trade vs ledger records, records mismatches, emits Kafka events, and surfaces metrics.
- **Scheduling**: `ReconciliationScheduler` orchestrates nightly ingestion/reconciliation and SLA escalation emails.
- **Security**: `SecurityConfig` enables JWT resource server and method-level role checks. Map Keycloak-style `realm_access.roles` into Spring Security authorities.
- **Observability**: Micrometer timers/counters for reconciliation runs (`reconciliation.run.*`) plus mismatch counters by type.

## Testing
- **Unit / Integration**: `ReconciliationServiceIntegrationTest` uses Testcontainers-backed PostgreSQL to validate reconciliation flow end-to-end.
- **Formatting**: Spotless (Google Java Format) enforced via `mvn spotless:apply` and CI check.
- **Profiles**: Tests run with `application-test.yaml` profile to keep logging quiet and disable batch autostart.

## CI/CD
GitHub Actions workflow (`.github/workflows/ci.yml`) compiles, runs tests, and enforces formatting on every push/PR targeting `main`. Easily extend with static analysis (SonarQube) or deployment stages.

## Next Steps
- Integrate with object storage SDK (S3/Azure) by providing a production `IngestionSourceProvider` implementation.
- Wire Kafka producer to Barclays schema registry and Avro/Protobuf schema evolution policies.
- Expand Testcontainers coverage for Kafka, add contract tests for REST endpoints, and include performance baselines.
- Harden reconciliation by supporting multi-leg trades, portfolio filters, and operator workflow audit trails.

## License
Internal project template for demonstration purposes.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Status

Compilable baseline, not an MVP yet. Before making changes:

1. Read `docs/architecture/10-implementation-memory.md` for verified implementation state.
2. Confirm the task against backlog IDs in `docs/architecture/08-backlog-implementation.md`.
3. Update `docs/architecture/10-implementation-memory.md` in the same PR when implementation status changes.

Mark backlog items `Done` only when code path is implemented and usable — not just scaffolded.

## Commands

```bash
# Build and test
./gradlew test

# Run the application (requires PostgreSQL via Docker Compose)
docker compose up -d
./gradlew bootRun --project-dir reporting-bootstrap

# Formatting — always apply before committing
./gradlew spotlessApply

# Verify formatting + tests (CI gate)
./gradlew spotlessCheck test

# Single module test
./gradlew :reporting-infrastructure:test

# Find key annotations
rg -n "@RestController|SecurityFilterChain|@Entity|Flyway" .
```

## Architecture

Modular monolith. Dependency direction: `bootstrap → application → domain`. Infrastructure and connectors implement application ports.

| Module | Role |
|---|---|
| `reporting-domain` | Core records, enums — no Spring, no framework deps |
| `reporting-application` | Use cases, DTOs, outbound ports — no Spring |
| `reporting-infrastructure` | Port implementations: validator, in-memory repo, Flyway migrations |
| `reporting-connectors` | Stub JDBC adapters: PostgreSQL, MySQL, Oracle via `ConnectorFactory` |
| `reporting-security` | `LocalAuthenticationProviderAdapter`, `AdAuthenticationProviderStub` |
| `reporting-jobs` | Placeholder export cleanup job |
| `reporting-bootstrap` | Spring Boot app, REST controllers, wiring, `application.yml` configs |

## Configuration

Default profile: `local`. All profiles: `local`, `dev`, `qa`, `prod`.

Environment variables for the operational PostgreSQL database:

| Variable | Default |
|---|---|
| `APP_DB_URL` | `jdbc:postgresql://localhost:5432/hile_reports` |
| `APP_DB_USERNAME` | `postgres` |
| `APP_DB_PASSWORD` | `postgres` |
| `APP_SERVER_PORT` | `8080` |
| `APP_SECURITY_MODE` | `local` |

Start local PostgreSQL: `docker compose up -d`. Flyway runs automatically on boot.

## Formatting

Java: `google-java-format`, 2-space indent. `Spotless` enforces all files (Java, Gradle, YAML, TOML, Properties). Run `./gradlew spotlessApply` before committing — Spotless wins over IDE formatters.

## Active Endpoints

- `POST /api/v1/auth/login` — local credential validation (no session/token yet)
- `GET /api/v1/architecture/modules` — architecture info

## Next Implementation Slice (in order)

1. Complete local auth: user/role/permission model, persistent users, session or JWT
2. Datasource CRUD with encrypted secret storage
3. Replace PostgreSQL/MySQL connector stubs with real JDBC adapters
4. Expose validation, column discovery, and preview endpoints

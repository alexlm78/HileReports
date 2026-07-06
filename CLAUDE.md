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
| `reporting-infrastructure` | Port implementations: JPA adapters for datasource, report, columns, params; Flyway migrations |
| `reporting-connectors` | Real JDBC adapters: PostgreSQL, MySQL; Oracle stub; `ConnectorFactory` |
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

| Method | Path | Role required |
|---|---|---|
| `POST` | `/api/v1/auth/login` | public |
| `GET` | `/api/v1/architecture/modules` | authenticated |
| `POST` | `/api/v1/categories` | `PLATFORM_ADMIN` |
| `GET` | `/api/v1/categories?page=0&size=20` | authenticated |
| `GET` | `/api/v1/categories/{id}` | authenticated |
| `PUT` | `/api/v1/categories/{id}` | `PLATFORM_ADMIN` |
| `DELETE` | `/api/v1/categories/{id}` | `PLATFORM_ADMIN` |
| `POST` | `/api/v1/datasources` | `PLATFORM_ADMIN` |
| `GET` | `/api/v1/datasources?page=0&size=20` | `PLATFORM_ADMIN` |
| `GET` | `/api/v1/datasources/{id}` | `PLATFORM_ADMIN` |
| `PUT` | `/api/v1/datasources/{id}` | `PLATFORM_ADMIN` |
| `DELETE` | `/api/v1/datasources/{id}` | `PLATFORM_ADMIN` |
| `POST` | `/api/v1/datasources/{id}/test` | `PLATFORM_ADMIN` |
| `POST` | `/api/v1/datasources/{id}/discover` | `PLATFORM_ADMIN` |
| `POST` | `/api/v1/datasources/{id}/preview` | `PLATFORM_ADMIN` |
| `POST` | `/api/v1/reports` | `PLATFORM_ADMIN` or `REPORT_DESIGNER` |
| `GET` | `/api/v1/reports?page=0&size=20&name=&status=&categoryId=` | authenticated |
| `GET` | `/api/v1/reports/{id}` | authenticated |
| `PUT` | `/api/v1/reports/{id}` | owner or `PLATFORM_ADMIN` |
| `DELETE` | `/api/v1/reports/{id}` | owner or `PLATFORM_ADMIN` (DRAFT only, no execution history) |
| `POST` | `/api/v1/reports/{id}/preview` | authenticated |
| `POST` | `/api/v1/reports/{id}/publish` | authenticated |
| `POST` | `/api/v1/reports/{id}/unpublish` | authenticated |
| `PUT` | `/api/v1/reports/{id}/columns` | authenticated |
| `GET` | `/api/v1/reports/{id}/columns` | authenticated |
| `PUT` | `/api/v1/reports/{id}/parameters` | authenticated |
| `GET` | `/api/v1/reports/{id}/parameters` | authenticated |
| `GET` | `/api/v1/catalog` | authenticated |
| `POST` | `/api/v1/reports/{id}/execute` | authenticated |
| `GET` | `/api/v1/reports/{id}/executions?page=0&size=20` | authenticated |
| `POST` | `/api/v1/reports/{id}/export` | authenticated |
| `GET` | `/api/v1/executions?page=0&size=20` | authenticated |
| `GET` | `/api/v1/exports?page=0&size=20` | authenticated |
| `GET` | `/api/v1/exports/{id}` | authenticated |
| `GET` | `/api/v1/exports/{id}/download` | authenticated |
| `POST` | `/api/v1/tags` | `PLATFORM_ADMIN` |
| `GET` | `/api/v1/tags` | authenticated |
| `DELETE` | `/api/v1/tags/{id}` | `PLATFORM_ADMIN` |
| `PUT` | `/api/v1/reports/{id}/tags` | owner or `PLATFORM_ADMIN` |
| `GET` | `/api/v1/reports/{id}/tags` | authenticated |
| `POST` | `/api/v1/users` | `PLATFORM_ADMIN` |
| `GET` | `/api/v1/users?page=0&size=20` | `PLATFORM_ADMIN` |
| `GET` | `/api/v1/users/{id}` | `PLATFORM_ADMIN` |
| `DELETE` | `/api/v1/users/{id}` | `PLATFORM_ADMIN` (soft-disable) |
| `PUT` | `/api/v1/users/{id}/password` | `PLATFORM_ADMIN` |
| `GET` | `/api/v1/users/me` | authenticated |
| `PUT` | `/api/v1/users/me/password` | authenticated |
| `GET` | `/api/v1/audit-events?actor=&action=&page=0&limit=100` | `PLATFORM_ADMIN` |

## Security Notes

- Default admin: `admin` / `admin123` — override immediately in non-local environments
- `APP_JWT_SECRET`: min 32 bytes (default dev key insecure)
- `APP_ENCRYPTION_SECRET`: AES-256-GCM key source (default dev key insecure)
- `APP_DB_SCHEMA`: defaults to `hile_reports` (used by Flyway and Hibernate)

## Next Implementation Slice (in order)

1. AD authentication (`TASK-02.3.1-b`) — deferred until live LDAP/AD is available for testing
2. Frontend / API client integration
3. Per-datasource ACL
4. Structured observability: correlation ID propagation to export async threads

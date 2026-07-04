# Hile Reports

Initial project baseline for the universal multi-DB reporting platform.

## Build

- Multi-module `Gradle` build.
- `settings.gradle` defines the workspace modules.
- `build.gradle` centralizes shared conventions and common versions.

## Modules

- `reporting-domain`: pure entities and business rules.
- `reporting-application`: use cases and ports.
- `reporting-infrastructure`: persistence and technical adapters.
- `reporting-connectors`: Oracle, MySQL, and PostgreSQL connectors.
- `reporting-security`: local authentication with a future path to `AD`.
- `reporting-jobs`: export and maintenance jobs.
- `reporting-bootstrap`: Spring Boot application and REST API.

## Documentation

Architecture and planning documentation lives under `docs/architecture/reporteador-universal`.

For AI or developer continuity, start with `AGENT_HANDOFF.md` and then `docs/architecture/10-implementation-memory.md`.

- English canonical files keep the original paths for compatibility.
- Spanish source versions are preserved alongside them with the `_es.md` suffix.

## Skeleton Goal

This baseline does not implement the full product yet; it defines module boundaries and the main contracts so the team can start implementation faster.

# Universal Reporting Platform

This documentation package defines the target architecture for a universal reporting system capable of executing SQL-based reports across multiple database engines, starting with Oracle, MySQL, and PostgreSQL.

## Goal

Enable administrator users to create reports from SQL queries, preview columns and data, define parameterized filters, and publish reports that are ready for execution by end users.

## Contents

- `00-vision-y-requerimientos.md`: context, goals, actors, and requirements.
- `01-opciones-arquitectonicas.md`: evaluated alternatives and recommendation.
- `02-arquitectura-recomendada.md`: target architecture, components, and diagrams.
- `03-modelo-datos-y-multidb.md`: domain model, metadata, and multi-DB strategy.
- `04-api-y-flujos.md`: API contracts and functional flows.
- `05-seguridad-operacion-y-nfr.md`: security, performance, observability, and operations.
- `06-plan-implementacion.md`: suggested phased roadmap.
- `07-plan-minucioso-implementacion.md`: detailed backlog by epics, tasks, dependencies, and deliverables.
- `10-implementation-memory.md`: verified implementation snapshot, progress, and pending work.
- `adr/ADR-001-monolito-modular.md`: baseline architecture decision.
- `adr/ADR-002-ejecucion-sql-segura.md`: decision on SQL validation and execution.
- `adr/ADR-003-conectividad-multidb.md`: decision on connectors and engine abstraction.
- `adr/ADR-004-stack-java-spring-boot.md`: baseline implementation stack decision.
- `adr/ADR-005-autenticacion-local-y-evolucion-ad.md`: staged authentication decision.

## Language Layout

- English remains in the original filenames for backwards compatibility.
- Spanish copies are preserved with the `_es.md` suffix.

## Initial Assumptions

- The system will be an internal enterprise web application.
- The initial business focus is internal operational reporting for specific information such as sales, transfers, and similar processes.
- The target stack will be `Java` with a preference for `Spring Boot`.
- The platform will be deployed `on-premise` first, while remaining ready for a future cloud migration.
- The first version will use local users, leaving a clear evolution path toward `Active Directory`.
- The platform must support up to `500` concurrent users as the initial capacity target.
- The first version will support only `SELECT` queries and `WITH` variants.
- Reports will run under permissions controlled by the system and by registered technical credentials.
- Preview will have a configurable row limit to avoid expensive workloads.
- The first release does not include a pixel-perfect designer; the focus is tabular reporting with export capabilities.
- End-user filters are based on report-declared parameters, not on free-form SQL editing.

## Confirmed Decisions

1. The system will be an internal platform for specific operational information across the company's business platforms.
2. The preferred stack is `Java`, prioritizing `Spring Boot` over `Quarkus` for the first release.
3. The target capacity is up to `500` concurrent users.
4. The initial deployment will be `on-premise`, with a low-budget approach and an architecture prepared to evolve to the cloud.
5. Initial authentication will be local, while `AD` integration is planned for a later phase.

## Executive Recommendation

The recommended approach is a **modular monolith** in `Java + Spring Boot`, with clearly separated layers, an asynchronous execution engine, and database connector abstractions. This option minimizes initial cost, improves SQL governance and security, supports the `500`-concurrent-user target, and preserves a clean path toward future service extraction or cloud migration if scale or organizational needs require it.

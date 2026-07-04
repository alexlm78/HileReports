# AGENT_HANDOFF

## Read This First

This repository is a `Java 21` + `Spring Boot 3.3.2` modular monolith for a multi-DB reporting platform.

Current reality:

- The repo is a **compilable baseline**, not a working MVP yet.
- Source of truth for target scope: `docs/architecture/07-detailed-implementation-plan.md`
- Source of truth for verified implementation status: `docs/architecture/10-implementation-memory.md`
- Main backlog reference: `docs/architecture/08-backlog-implementation.md`

Before making changes:

1. Read `docs/architecture/10-implementation-memory.md`.
2. Confirm the task against the backlog IDs in `docs/architecture/08-backlog-implementation.md`.
3. Prefer continuing the next vertical slice instead of expanding unrelated scaffolding.

## Current State

Implemented baseline:

- Multi-module `Gradle` build
- Modules:
  - `reporting-domain`
  - `reporting-application`
  - `reporting-infrastructure`
  - `reporting-connectors`
  - `reporting-security`
  - `reporting-jobs`
  - `reporting-bootstrap`
- Minimal domain/application contracts
- Simple read-only SQL validator with tests
- Stub DB connectors for PostgreSQL, MySQL, and Oracle
- Decoupled auth port with local adapter and `AD` stub
- Spring Boot bootstrap with one architecture endpoint
- Minimal local HTTP auth slice with `Spring Security` and `POST /api/v1/auth/login`

Not implemented yet:

- Persistent auth model with users, roles, and permissions
- Session or token-based authentication
- `Flyway` migrations
- `JPA` persistence
- Datasource CRUD
- Real connector execution
- Report publication/versioning workflow
- Catalog, execution, auditing, exports

## Fast Repository Map

- `reporting-domain`: core records/enums
- `reporting-application`: use cases, DTOs, ports
- `reporting-infrastructure`: validator and in-memory repository
- `reporting-connectors`: connector factory and stub adapters
- `reporting-security`: local auth adapter and future AD stub
- `reporting-jobs`: placeholder export cleanup job
- `reporting-bootstrap`: Spring Boot app and REST controllers

## Verified Working Flow

These narrow flows are implemented:

1. Build a `CreateReportDefinitionCommand`
2. Validate SQL with `SimpleReadOnlyQueryValidator`
3. Save a draft `ReportDefinition` through `InMemoryReportDefinitionRepository`
4. Authenticate local credentials through `POST /api/v1/auth/login`

The report draft flow is still not exposed as a complete REST feature, and the auth slice still has no reusable session or token.

## Recommended Next Slice

Follow this order unless the user explicitly redirects:

1. Add real environment configuration and persistence base
   - profiles `local/dev/qa/prod`
   - PostgreSQL operational database
   - `Flyway`
2. Complete local auth beyond the initial HTTP slice
   - user/role/permission model
   - persistent users
   - session or JWT
3. Implement datasource CRUD with encrypted secret storage
4. Replace PostgreSQL/MySQL connector stubs with real JDBC adapters
5. Expose validation, discovery, and preview endpoints

## Rules for Future Changes

- Treat `docs/architecture/10-implementation-memory.md` as the detailed status document.
- Update that memory file in the same PR when implementation status changes.
- Mark backlog items as done only when they are actually usable, not when they are only scaffolded.
- Do not assume a stub adapter means the feature exists.
- Run at least:

```powershell
./gradlew test
```

## Useful Commands

```powershell
./gradlew test
./gradlew bootRun --project-dir reporting-bootstrap
rg --files .
rg -n "@RestController|SecurityFilterChain|@Entity|Flyway" .
```

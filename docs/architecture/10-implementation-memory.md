# Implementation Memory

## Purpose

This document gives an AI agent or a new developer a verified snapshot of the current implementation state without needing to read the entire repository. It complements the target architecture and backlog documents with the real status of the codebase.

## Last Verified Snapshot

- Date: `2026-07-03`
- Repository status: clean working tree at verification time
- Build status: `./gradlew test` passes
- Scope of verification: source tree, Gradle modules, Spring Boot bootstrap, tests, and backlog alignment

## Executive Summary

The repository is currently a **compilable baseline** for the product, not an MVP implementation yet.

What is already in place:

- Multi-module `Gradle` structure aligned with the modular monolith design.
- Shared build conventions for `Java 21`, `Spring Boot 3.3.2`, and formatting via `Spotless`.
- Minimal domain, application, infrastructure, connectors, security, jobs, and bootstrap modules.
- A simple read-only SQL validator with a small test suite.
- Connector abstractions and engine-specific stub adapters for PostgreSQL, MySQL, and Oracle.
- A decoupled authentication port with a local adapter and an `AD` stub.

What is not in place yet:

- No real authentication flow over HTTP.
- No `Spring Security` web configuration.
- No persistence with `JPA` or `Flyway`.
- No datasource CRUD.
- No real preview against databases.
- No report publication, catalog, execution, auditing, or exports.
- No frontend module.

## Verified Implementation by Module

### `reporting-domain`

Implemented:

- `ReportDefinition`
- `ReportStatus`
- `DataSourceType`
- `AuthenticatedUser`

Current assessment:

- The domain is still minimal and only covers the first report and security concepts.
- There are no entities or value objects for users, roles, permissions, categories, datasource metadata, executions, exports, or auditing.

### `reporting-application`

Implemented:

- `CreateReportDefinitionCommand`
- `ValidationResult`
- `ColumnMetadata`
- `CreateReportDefinitionUseCase`
- Outbound ports:
  - `AuthenticationProviderPort`
  - `DbConnectorPort`
  - `QueryValidatorPort`
  - `ReportDefinitionRepository`
- `ReportDefinitionApplicationService`

Current assessment:

- The only implemented use case is `createDraft`.
- `createDraft` validates SQL through the validator port and persists a draft report through the repository port.
- There are no use cases for login, datasource management, preview orchestration, publish/unpublish, catalog, execution, auditing, or exports.

### `reporting-infrastructure`

Implemented:

- `SimpleReadOnlyQueryValidator`
- `InMemoryReportDefinitionRepository`
- Validator tests

Current assessment:

- The validator is intentionally simple. It allows `SELECT` and `WITH`, blocks several DDL/DML tokens and semicolons, and extracts named parameters with regex.
- Persistence is in-memory only.
- There are no migrations, no database configuration, no `JPA` entities, no repositories backed by PostgreSQL, and no auditing infrastructure.

### `reporting-connectors`

Implemented:

- `ConnectorFactory`
- `PostgreSqlConnectorAdapter`
- `MySqlConnectorAdapter`
- `OracleConnectorAdapter`
- Shared `BaseStubConnector`

Current assessment:

- All connector adapters are stubs.
- `testConnection` only validates that `jdbcUrl` is not blank.
- `discoverColumns` returns sample metadata.
- `executePreview` returns a fake sample row.
- No real JDBC connectivity, pooling, dialect handling, limits, or error mapping is implemented.

### `reporting-security`

Implemented:

- `LocalAuthenticationProviderAdapter`
- `AdAuthenticationProviderStub`

Current assessment:

- Authentication is decoupled behind a port, which is good groundwork for later `AD` support.
- Local auth is currently a hardcoded in-memory credential map with `BCrypt`.
- There is no user repository, no role or permission model, no token/session handling, and no HTTP security integration.

### `reporting-jobs`

Implemented:

- `ExportCleanupJob`

Current assessment:

- This is a placeholder only.
- There is no scheduler, no export queue, no file lifecycle, and no persisted job state.

### `reporting-bootstrap`

Implemented:

- `HileReportsApplication`
- `ArchitectureController`
- Base `application.yml`

Current assessment:

- The only exposed HTTP endpoint is `GET /api/v1/architecture/modules`.
- `application.yml` defines the app name, server port, actuator exposure, preview row limit, and execution timeout defaults.
- There are no environment-specific profiles (`local`, `dev`, `qa`, `prod`).
- There are no beans wiring application services, adapters, or connector factory into usable APIs.

## Backlog Alignment

Status legend:

- `Done`: implemented with usable code in the repo
- `Partial`: present as baseline, stub, or incomplete slice
- `Not started`: not found in code

| Backlog Item | Status | Notes |
| --- | --- | --- |
| `TASK-01.1.1-a` Create root build and modules | Done | Multi-module `Gradle` structure exists |
| `TASK-01.1.1-b` Java version and Spring Boot BOM | Done | `Java 21` and Boot BOM configured |
| `TASK-01.1.1-c` Build and test plugins | Done | Shared Gradle conventions and tests configured |
| `TASK-01.2.1-a` Base config and profiles | Partial | Base `application.yml` exists, profiles do not |
| `TASK-01.2.1-b` Externalize sensitive variables | Not started | No evidence in repo |
| `TASK-01.3.1-a` CI pipeline | Not started | No pipeline files found |
| `TASK-01.3.1-b` Publish executable artifact | Partial | Boot app can build locally, no pipeline/release flow |
| `TASK-02.1.1-a` User, role, permission entities | Not started | Only `AuthenticatedUser` record exists |
| `TASK-02.1.1-b` Spring Security and hashing | Partial | `BCrypt` used, but no `Spring Security` web config |
| `TASK-02.1.1-c` Authentication endpoint | Not started | No auth controller |
| `TASK-02.2.1-a` Initial permission matrix | Not started | No permission model |
| `TASK-02.2.1-b` Authorization by endpoint | Not started | No secured endpoints |
| `TASK-02.2.1-c` Permissions by report and datasource | Not started | No ACL implementation |
| `TASK-02.3.1-a` Decoupled authentication port | Done | Port and local/AD adapters exist |
| `TASK-03.1.1-a` Flyway migrations | Not started | No migration files or Flyway setup |
| `TASK-03.1.1-b` Repositories and base services | Partial | Only in-memory report repository plus one app service |
| `TASK-03.1.1-c` Entity auditing | Not started | No auditing model or infrastructure |
| `TASK-03.2.1-a` Category CRUD | Not started | No category module or endpoint |
| `TASK-03.2.1-b` Tag and ownership model | Not started | No implementation |
| `TASK-04.1.1-a` `data_source` CRUD | Not started | No datasource service or API |
| `TASK-04.1.1-b` Secret encryption | Not started | No secret storage or encryption logic |
| `TASK-04.1.1-c` `testConnection` | Partial | Present as stub on connector contract |
| `TASK-04.2.1-a` `PostgreSqlConnector` | Partial | Stub only |
| `TASK-04.2.1-b` `MySqlConnector` | Partial | Stub only |
| `TASK-04.2.1-c` `OracleConnector` | Partial | Stub only |
| `TASK-04.2.1-d` `ConnectorFactory` | Partial | Factory exists, but not wired to real adapters or API |
| `TASK-05.1.1-a` `QueryValidator` | Partial | Simple implementation exists |
| `TASK-05.1.1-b` Block DDL, DML, multiple statements | Partial | Basic token blocking exists |
| `TASK-05.1.1-c` Detect dangerous patterns and comments | Not started | No comment/pattern analysis found |
| `TASK-05.1.1-d` Extract named parameters | Done | Regex-based extraction implemented |
| `TASK-05.2.1-a` Matrix of valid and invalid cases | Partial | Only a few tests exist |
| `TASK-05.2.1-b` Tests by dialect | Not started | No dialect-specific tests |
| `TASK-06.1.1-a` `discoverColumns` | Partial | Stub implementation only |
| `TASK-06.1.1-b` Standardize output types | Partial | `ColumnMetadata` exists, types are sample strings |
| `TASK-06.2.1-a` `executePreview` with limits | Partial | Stub implementation only |
| `TASK-06.2.1-b` Preview endpoint | Not started | No preview controller |
| `TASK-07.1.1-a` `ReportDefinitionService` | Partial | Draft creation only |
| `TASK-07.1.1-b` Save draft and create new version | Partial | Draft save exists, versioning does not |
| `TASK-07.1.1-c` Publish and unpublish reports | Not started | No publish flow |
| `TASK-07.2.1-a` Configure columns | Not started | No column config persisted |
| `TASK-07.2.1-b` Configure parameters | Not started | No parameter config persisted |
| `TASK-07.2.1-c` Publish only after successful preview | Not started | No publish workflow |
| `EP-08` Catalog and execution | Not started | No catalog or runtime execution flow |
| `EP-09` Exports | Not started | Placeholder job only |
| `EP-10` Observability and hardening | Partial | Actuator exposure exists, rest missing |

## Working Vertical Slice Already Available

The current repository already supports this narrow internal flow at code level:

1. Create a `CreateReportDefinitionCommand`.
2. Validate SQL as read-only through `SimpleReadOnlyQueryValidator`.
3. Persist a `ReportDefinition` draft into `InMemoryReportDefinitionRepository`.

Important limitation:

- This flow is not exposed as a REST API and is not wired through Spring beans by default.

## Major Gaps Before the MVP Path Works End to End

The architecture recommends the shortest value path:

`local login -> PostgreSQL/MySQL datasource -> secure SQL -> preview -> builder -> catalog -> execution`

Today the main blockers are:

1. No real datasource registration and no secret handling.
2. No real connector execution against databases.
3. No auth endpoint or HTTP security.
4. No metadata persistence database.
5. No preview API.
6. No report publication or catalog.

## Recommended Next Implementation Slice

The most pragmatic next slice is:

1. Add real environment configuration and persistence base:
   - `local/dev/qa/prod` profiles
   - `Flyway`
   - PostgreSQL metadata database
2. Implement local auth over HTTP:
   - user/role/permission model
   - security configuration
   - login endpoint
3. Implement datasource CRUD plus encrypted secret storage.
4. Replace stub PostgreSQL/MySQL connectors with real JDBC-based adapters.
5. Expose validation, column discovery, and preview endpoints.
6. Only then expand builder versioning and publication.

## Commands Used to Verify the Snapshot

```powershell
git status --short
./gradlew test
rg --files .
rg -n "@RestController|SecurityFilterChain|@Entity|Flyway" .
```

## Update Protocol for Future Agents

When implementation changes, update this file in the same PR and keep these sections current:

1. `Last Verified Snapshot`
2. `Verified Implementation by Module`
3. `Backlog Alignment`
4. `Recommended Next Implementation Slice`

Rules:

- Mark an item as `Done` only when the code path is implemented and usable, not just scaffolded.
- Mark an item as `Partial` when it exists only as a stub, placeholder, in-memory adapter, or non-exposed internal code path.
- Prefer linking changes to backlog IDs.
- Always rerun at least `./gradlew test` before updating the snapshot.

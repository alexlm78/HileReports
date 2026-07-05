# Implementation Memory

## Purpose

This document gives an AI agent or a new developer a verified snapshot of the current implementation state without needing to read the entire repository. It complements the target architecture and backlog documents with the real status of the codebase.

## Last Verified Snapshot

- Date: `2026-07-04`
- Repository status: Full EP-07 builder — report preview gate, publish/unpublish, column config, parameter config; all backed by JPA
- Build status: `./gradlew test` passes
- Scope of verification: source tree, Gradle modules, Spring Boot bootstrap, tests, and backlog alignment

## Executive Summary

The repository is currently an **active R1 MVP baseline** — local security slice is now substantially complete.

What is already in place:

- Multi-module `Gradle` structure aligned with the modular monolith design.
- Shared build conventions for `Java 21`, `Spring Boot 3.3.2`, and formatting via `Spotless`.
- Minimal domain, application, infrastructure, connectors, security, jobs, and bootstrap modules.
- Environment-aware Spring Boot configuration with `local`, `dev`, `qa`, and `prod` profiles.
- Externalized operational database settings through environment variables.
- `Flyway` integration with two migrations: operational metadata schema (V1) and security schema (V2).
- A local `compose.yaml` for the operational PostgreSQL database used by the application itself.
- A simple read-only SQL validator with a small test suite.
- Connector abstractions and engine-specific stub adapters for PostgreSQL, MySQL, and Oracle.
- A decoupled authentication port backed by persistent users loaded from PostgreSQL via JPA.
- JWT-based stateless authentication: `POST /api/v1/auth/login` returns a signed JWT token.
- `JwtAuthenticationFilter` protects all non-public endpoints via `Authorization: Bearer <token>`.
- `SecurityDataInitializer` creates the default admin user on first boot.
- `app_role`, `app_permission`, `user_role`, `role_permission` tables seeded with permissions and three initial roles.
- RBAC URL rules: `/api/v1/datasources/**` requires `ROLE_PLATFORM_ADMIN`.
- Datasource CRUD: create, list, get, delete, test-connection endpoints under `/api/v1/datasources`.
- AES-256-GCM password encryption; encrypted values stored as `enc:v1:<iv>:<ciphertext>` in `secret_ref`.
- `ConnectorFactory` wired to stub adapters for PostgreSQL, MySQL, and Oracle.
- `DataSourceApplicationService` builds JDBC URL, decrypts secret, calls connector for test/discover/preview.
- `POST /api/v1/datasources/{id}/discover` — discovers columns from SQL text against real DB.
- `POST /api/v1/datasources/{id}/preview` — executes SQL against real DB, returns columns + rows (capped by `hile.reports.preview.max-rows`).
- Report builder: `POST /api/v1/reports`, `GET /api/v1/reports`, `GET /api/v1/reports/{id}`.
- `POST /api/v1/reports/{id}/preview` — runs report SQL against its datasource, updates `report_version.preview_status=VALID|INVALID`.
- `POST /api/v1/reports/{id}/publish` — transitions DRAFT → PUBLISHED; rejects if `preview_status != VALID`.
- `POST /api/v1/reports/{id}/unpublish` — transitions PUBLISHED → DRAFT.
- `PUT /api/v1/reports/{id}/columns` — replace-all column config in `report_column` for current version.
- `GET /api/v1/reports/{id}/columns` — list configured columns (ordered by ordinal).
- `PUT /api/v1/reports/{id}/parameters` — replace-all parameter config in `report_parameter`.
- `GET /api/v1/reports/{id}/parameters` — list configured parameters.
- `ReportColumnEntity`, `ReportParameterEntity` JPA entities added.
- `InMemoryReportDefinitionRepository` removed; replaced by `ReportDefinitionRepositoryAdapter` (JPA).

What is not in place yet:

- No report publication, catalog, execution, auditing, or exports.
- No frontend module.
- No per-report or per-datasource ACL.
- No column/parameter configuration for report versions.
- Oracle connector still a stub (no freely distributable JDBC driver on Maven Central).

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
- `Flyway` V1 migration for operational metadata schema
- `Flyway` V2 migration for security schema (`app_user`, `app_role`, `app_permission`, `user_role`, `role_permission`) with seeded permissions and roles
- `AppUserEntity`, `AppRoleEntity` JPA entities
- `AppUserJpaRepository`, `AppRoleJpaRepository` Spring Data repositories
- `UserRepositoryAdapter` implementing `UserRepositoryPort`
- Validator tests

Current assessment:

- The validator is intentionally simple. It allows `SELECT` and `WITH`, blocks several DDL/DML tokens and semicolons, and extracts named parameters with regex.
- The report repository is still in-memory only.
- User persistence is now fully backed by PostgreSQL and JPA.
- No `JPA` entities for report definitions, datasources, or categories yet.

### `reporting-connectors`

Implemented:

- `ConnectorFactory`
- `PostgreSqlConnectorAdapter` — real JDBC via `DriverManager`
- `MySqlConnectorAdapter` — real JDBC via `DriverManager`
- `OracleConnectorAdapter` — still a stub (no Oracle JDBC driver added)
- Shared `BaseStubConnector` (used by Oracle only now)

Current assessment:

- PostgreSQL and MySQL connectors use `DriverManager.getConnection` for `testConnection`, `discoverColumns` (executes with `maxRows=1`, reads `ResultSetMetaData`), and `executePreview` (executes with `setMaxRows(limit)`).
- `DbConnectorPort.discoverColumns` and `executePreview` now take JDBC connection params (URL, username, rawPassword).
- JDBC drivers added as `runtimeOnly` in `reporting-connectors/build.gradle`: `org.postgresql:postgresql`, `com.mysql:mysql-connector-j`.

### `reporting-security`

Implemented:

- `LocalAuthenticationProviderAdapter` — loads users from DB via `UserRepositoryPort`
- `AdAuthenticationProviderStub`
- `BCrypt` password hashing
- `JwtTokenProvider` / `TokenProviderPort` — generates and validates HS256 JWTs
- JWT secret and expiration externalized through `APP_JWT_SECRET` and `APP_JWT_EXPIRATION_MS`

Current assessment:

- Authentication is decoupled behind a port, which is good groundwork for later `AD` support.
- Local auth reads users from PostgreSQL via JPA. In-memory hardcoded credentials are removed.
- JWT tokens carry username and roles. Expiration defaults to 24 hours.
- No role-based endpoint protection yet (RBAC wiring is the next step).

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
- `AuthController` — returns JWT token on successful login
- Base `application.yml`
- Profile-specific configuration for `local`, `dev`, `qa`, and `prod`
- JPA, `Flyway`, and JDBC runtime configuration for the operational PostgreSQL database
- Local Docker Compose definition for the operational PostgreSQL instance
- `SecurityConfig` with JWT filter and stateless session
- `JwtAuthenticationFilter` — validates `Authorization: Bearer <token>` on every request
- `SecurityDataInitializer` — creates default admin user on first boot
- `JpaConfig` — registers JPA repository scan for `dev.kreaker.hile` package tree
- Bean wiring for local authentication with `UserRepositoryPort`

Current assessment:

- `POST /api/v1/auth/login` returns `{ username, roles, token, expiresInMs, authenticationMode }`.
- All non-public endpoints require a valid JWT.
- Default admin credentials: `admin` / `admin123` — change immediately in any non-local environment.
- JWT secret must be overridden via `APP_JWT_SECRET` (min 32 bytes) in non-local environments.
- There are still no beans wiring preview, datasource CRUD, or real connectors into usable APIs.

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
| `TASK-01.2.1-a` Base config and profiles | Done | Base config plus `local`, `dev`, `qa`, and `prod` profiles exist |
| `TASK-01.2.1-b` Externalize sensitive variables | Done | Operational datasource settings are externalized through environment variables |
| `TASK-01.3.1-a` CI pipeline | Not started | No pipeline files found |
| `TASK-01.3.1-b` Publish executable artifact | Partial | Boot app can build locally, no pipeline/release flow |
| `TASK-02.1.1-a` User, role, permission entities | Done | `AppUserEntity`, `AppRoleEntity`, `AppPermissionEntity` (SQL) + V2 migration with seeded roles and permissions |
| `TASK-02.1.1-b` Spring Security and hashing | Done | `BCrypt`, `Spring Security` stateless config, JWT filter, persistent user repository |
| `TASK-02.1.1-c` Authentication endpoint | Done | `POST /api/v1/auth/login` returns signed JWT with roles |
| `TASK-02.2.1-a` Initial permission matrix | Done | Permission rows seeded in DB; role↔permission matrix in `role_permission` table |
| `TASK-02.2.1-b` Authorization by endpoint | Done | URL-based RBAC in `SecurityConfig`; datasource endpoints require `PLATFORM_ADMIN` |
| `TASK-02.2.1-c` Permissions by report and datasource | Not started | No ACL implementation |
| `TASK-02.3.1-a` Decoupled authentication port | Done | Port and local/AD adapters exist |
| `TASK-03.1.1-a` Flyway migrations | Done | V1 operational metadata + V2 security schema both configured and applied |
| `TASK-03.1.1-b` Repositories and base services | Partial | Only in-memory report repository plus one app service |
| `TASK-03.1.1-c` Entity auditing | Not started | No auditing model or infrastructure |
| `TASK-03.2.1-a` Category CRUD | Not started | No category module or endpoint |
| `TASK-03.2.1-b` Tag and ownership model | Not started | No implementation |
| `TASK-04.1.1-a` `data_source` CRUD | Done | `DataSourceController` with create/list/get/delete endpoints wired to `DataSourceApplicationService` |
| `TASK-04.1.1-b` Secret encryption | Done | `AesGcmEncryptor` (AES-256-GCM); encrypted in `secret_ref`; decrypted only for connection test |
| `TASK-04.1.1-c` `testConnection` | Done | `POST /api/v1/datasources/{id}/test` decrypts secret and delegates to connector stub |
| `TASK-04.2.1-a` `PostgreSqlConnector` | Done | Real JDBC testConnection, discoverColumns, executePreview |
| `TASK-04.2.1-b` `MySqlConnector` | Done | Real JDBC testConnection, discoverColumns, executePreview |
| `TASK-04.2.1-c` `OracleConnector` | Partial | Stub only — no Oracle JDBC driver on Maven Central |
| `TASK-04.2.1-d` `ConnectorFactory` | Done | Factory wired to real adapters; discover/preview exposed via REST |
| `TASK-05.1.1-a` `QueryValidator` | Partial | Simple implementation exists |
| `TASK-05.1.1-b` Block DDL, DML, multiple statements | Partial | Basic token blocking exists |
| `TASK-05.1.1-c` Detect dangerous patterns and comments | Not started | No comment/pattern analysis found |
| `TASK-05.1.1-d` Extract named parameters | Done | Regex-based extraction implemented |
| `TASK-05.2.1-a` Matrix of valid and invalid cases | Partial | Only a few tests exist |
| `TASK-05.2.1-b` Tests by dialect | Not started | No dialect-specific tests |
| `TASK-06.1.1-a` `discoverColumns` | Done | Real JDBC via `ResultSetMetaData` for PG/MySQL |
| `TASK-06.1.1-b` Standardize output types | Done | `ColumnMetadata(sourceName, label, dataType)` with driver-native type name |
| `TASK-06.2.1-a` `executePreview` with limits | Done | Real JDBC with `setMaxRows(limit)` for PG/MySQL |
| `TASK-06.2.1-b` Preview endpoint | Done | `POST /api/v1/datasources/{id}/preview` + discover endpoint |
| `TASK-07.1.1-a` `ReportDefinitionService` | Done | Create, findById, findAll wired to JPA |
| `TASK-07.1.1-b` Save draft and create new version | Done | Adapter saves report_definition + report_version atomically |
| `TASK-07.1.1-c` Publish and unpublish reports | Done | `POST /{id}/publish` (gates on VALID preview), `POST /{id}/unpublish` (PUBLISHED → DRAFT) |
| `TASK-07.2.1-a` Configure columns | Done | `PUT /api/v1/reports/{id}/columns` — replaces all columns for current version in `report_column` |
| `TASK-07.2.1-b` Configure parameters | Done | `PUT /api/v1/reports/{id}/parameters` — replaces all params in `report_parameter` |
| `TASK-07.2.1-c` Publish only after successful preview | Done | `POST /{id}/preview` updates `report_version.preview_status=VALID`; publish rejects if not VALID |
| `EP-08` Catalog and execution | Not started | No catalog or runtime execution flow |
| `EP-09` Exports | Not started | Placeholder job only |
| `EP-10` Observability and hardening | Partial | Actuator exposure exists, rest missing |

## Working Vertical Slice Already Available

The current repository already supports this narrow internal flow at code level:

1. Create a `CreateReportDefinitionCommand`.
2. Validate SQL as read-only through `SimpleReadOnlyQueryValidator`.
3. Persist a `ReportDefinition` draft into `InMemoryReportDefinitionRepository`.
4. Authenticate local credentials through `POST /api/v1/auth/login`.

Important limitation:

- The report draft flow is still not exposed as a REST API.
- The HTTP login flow does not create a persistent session or return a reusable token.

## Major Gaps Before the MVP Path Works End to End

The architecture recommends the shortest value path:

`local login -> PostgreSQL/MySQL datasource -> secure SQL -> preview -> builder -> catalog -> execution`

Today the main blockers are:

1. No real datasource registration and no secret handling.
2. No real connector execution against databases.
3. No persistent user, role, or permission model.
4. No application metadata persistence wired into real repositories yet.
5. No preview API.
6. No report publication or catalog.

## Recommended Next Implementation Slice

1. **Catalog endpoint** (`TASK-08.1.1-a`): `GET /api/v1/catalog` — filterable list of PUBLISHED reports accessible to authenticated users; excludes DRAFT/ARCHIVED.
2. **Per-report ACL** (`TASK-02.2.1-c`, `TASK-08.1.1-b`): ownership model — creator owns report; PLATFORM_ADMIN can manage all.
3. **Parameterized execution** (`TASK-08.2.1-a`, `TASK-08.2.1-b`, `TASK-08.2.1-c`): resolve `report_parameter` bindings, execute paginated query, persist `report_execution` + `report_execution_parameter`.

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

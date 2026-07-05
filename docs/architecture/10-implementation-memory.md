# Implementation Memory

## Purpose

This document gives an AI agent or a new developer a verified snapshot of the current implementation state without needing to read the entire repository. It complements the target architecture and backlog documents with the real status of the codebase.

## Last Verified Snapshot

- Date: `2026-07-05`
- Repository status: EP-08 done; EP-09 done; TASK-10.1.1-a/c done; CI pipeline added (TASK-01.3.1-a); TASK-03.2.1-b (Tag model) done
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
- `GET /api/v1/catalog` — returns PUBLISHED reports only; optional `?name=` case-insensitive substring filter; ordered by `created_at DESC`.
- `ReportSecurityGuard` — `@Component("reportSecurity")` that checks report ownership or `ROLE_PLATFORM_ADMIN`. Used via `@PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")` on publish/unpublish/preview/columns/parameters endpoints.
- `POST /api/v1/reports/{id}/execute` — executes a PUBLISHED report with optional parameter map and pagination (`page`, `pageSize`). Named params (`:paramName`) in SQL text substituted with typed values from `report_parameter` metadata. Appends `LIMIT ? OFFSET ?` for pagination (PostgreSQL/MySQL). Persists `report_execution` + `report_execution_parameter` rows with COMPLETED or FAILED status, duration, and row count. Returns `ExecutionResultView` with `executionId`, `correlationId`, `columns`, `rows`, `rowCount`, `durationMs`, `page`, `pageSize`.
- `ReportDefinition` domain record gained `currentVersionId UUID` field (used internally by execution layer).
- Category CRUD: `POST /api/v1/categories` (`PLATFORM_ADMIN`), `GET /api/v1/categories`, `GET /api/v1/categories/{id}`, `DELETE /api/v1/categories/{id}` (`PLATFORM_ADMIN`). Name uniqueness enforced. `category_id` optional FK on `report_definition` — passed in `POST /api/v1/reports` body.
- Async CSV/XLSX export: `POST /api/v1/reports/{id}/export` (202 Accepted, format CSV|XLSX, optional params + pageSize). Creates `report_execution` (RUNNING/ASYNC) + `report_export` (PENDING) synchronously; hands off to `@Async("exportTaskExecutor")`. `GET /api/v1/exports/{id}` polls status. `GET /api/v1/exports/{id}/download` streams file when COMPLETED, 409 if running, 404 if missing.
- `ExportCleanupScheduler` (`@Scheduled`, default 1h): deletes expired export files and DB records.
- `AsyncConfig`: `@EnableAsync @EnableScheduling`; `exportTaskExecutor` pool (core=2, max=5, queue=50).
- CSV via `commons-csv:1.11.0`; XLSX via `poi-ooxml:5.3.0`.
- Export storage path, expiry hours, and cleanup intervals configurable via env vars.

What is not in place yet:

- No frontend module.
- No per-datasource ACL.
- Oracle connector still a stub (no freely distributable JDBC driver on Maven Central).
- No structured observability (correlation ID propagation, Micrometer metrics beyond Actuator exposure).

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

- Full report lifecycle use cases: createDraft, findById, findAll, runPreview, publish, unpublish, upsertColumns, getColumns, upsertParameters, getParameters, getCatalog.
- `ExecuteReportUseCase` with named-param binding, pagination, and execution history persistence.
- `ExportJobUseCase` / `ExportJobApplicationService`: create export job, poll status, get file path.
- `CategoryUseCase` / `CategoryApplicationService`: category CRUD with name uniqueness.
- `NamedParamBinder`: package-private utility shared by execute and export services.
- `TagUseCase` / `TagApplicationService`: tag CRUD (create with slug derivation + uniqueness, findAll, delete) and report-tag association (`setReportTags` with tag existence validation, `getReportTags`).

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
- JPA adapters exist for datasource, report definition+version, report columns, report parameters, report executions, and tags.
- User persistence fully backed by PostgreSQL and JPA.
- JPA entities for categories (`CategoryEntity`), exports (`ReportExportEntity`), and tags (`TagEntity`, `ReportTagEntity`).
- Flyway migrations: V3 (category, report_execution, report_execution_parameter, report_export); V4 (`tag` + `report_tag` with composite PK and cascade-delete FKs).

### `reporting-connectors`

Implemented:

- `ConnectorFactory`
- `PostgreSqlConnectorAdapter` — real JDBC via `DriverManager`
- `MySqlConnectorAdapter` — real JDBC via `DriverManager`
- `OracleConnectorAdapter` — still a stub (no Oracle JDBC driver added)
- Shared `BaseStubConnector` (used by Oracle only now)

Current assessment:

- PostgreSQL and MySQL connectors use `DriverManager.getConnection` for `testConnection`, `discoverColumns` (executes with `maxRows=1`, reads `ResultSetMetaData`), `executePreview` (executes with `setMaxRows(limit)`), and `executeWithParams` (named-param SQL + LIMIT/OFFSET appended for pagination).
- `DbConnectorPort` now includes `executeWithParams(jdbcUrl, username, rawPassword, sql, paramValues, pageSize, offset) → PreviewResult`.
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
| `TASK-01.3.1-a` CI pipeline | Done | `.github/workflows/ci.yml` — push+PR to main; Java 21 temurin; `spotlessCheck test`; test report artifact on failure |
| `TASK-01.3.1-b` Publish executable artifact | Done | `publish` job in CI; multi-stage Dockerfile (temurin:21-jdk-alpine builder + jre-alpine runtime); pushes to ghcr.io on main push (short SHA + latest tags) |
| `TASK-02.1.1-a` User, role, permission entities | Done | `AppUserEntity`, `AppRoleEntity`, `AppPermissionEntity` (SQL) + V2 migration with seeded roles and permissions |
| `TASK-02.1.1-b` Spring Security and hashing | Done | `BCrypt`, `Spring Security` stateless config, JWT filter, persistent user repository |
| `TASK-02.1.1-c` Authentication endpoint | Done | `POST /api/v1/auth/login` returns signed JWT with roles |
| `TASK-02.2.1-a` Initial permission matrix | Done | Permission rows seeded in DB; role↔permission matrix in `role_permission` table |
| `TASK-02.2.1-b` Authorization by endpoint | Done | URL-based RBAC in `SecurityConfig`; datasource endpoints require `PLATFORM_ADMIN` |
| `TASK-02.2.1-c` Permissions by report and datasource | Done | Execute/export gated to REPORT_EXECUTE roles (PLATFORM_ADMIN, REPORT_DESIGNER, REPORT_VIEWER); design ops (/reports/**) restricted to REPORT_DESIGNER+; catalog + exports same gate; `canExecute(auth)` on `ReportSecurityGuard` |
| `TASK-02.3.1-a` Decoupled authentication port | Done | Port and local/AD adapters exist |
| `TASK-03.1.1-a` Flyway migrations | Done | V1 operational metadata + V2 security schema both configured and applied |
| `TASK-03.1.1-b` Repositories and base services | Done | Full JPA adapters for all entities; InMemoryReportDefinitionRepository removed |
| `TASK-03.1.1-c` Entity auditing | Done | `AuditableEntity` @MappedSuperclass (@LastModifiedDate updatedAt, @LastModifiedBy updatedBy); `ReportDefinitionEntity` + `ReportVersionEntity` extend it; `@EnableJpaAuditing` + `AuditorAware` from SecurityContextHolder in `JpaConfig`; V3 migration adds nullable `updated_at`/`updated_by` columns |
| `TASK-03.2.1-a` Category CRUD | Done | `CategoryEntity`, `CategoryRepositoryAdapter`, `CategoryApplicationService`, `CategoryController`; name uniqueness; `categoryId` FK on report |
| `TASK-03.2.1-b` Tag and ownership model | Done | `Tag` domain record; `TagUseCase`/`TagApplicationService`; `TagEntity`+`ReportTagEntity` (composite PK, cascade delete); `TagController` (`POST/GET/DELETE /api/v1/tags`); `PUT /api/v1/reports/{id}/tags` + `GET /api/v1/reports/{id}/tags` in `ReportController`; V4 Flyway migration; slug auto-derived from name |
| `TASK-04.1.1-a` `data_source` CRUD | Done | `DataSourceController` with create/list/get/delete endpoints wired to `DataSourceApplicationService` |
| `TASK-04.1.1-b` Secret encryption | Done | `AesGcmEncryptor` (AES-256-GCM); encrypted in `secret_ref`; decrypted only for connection test |
| `TASK-04.1.1-c` `testConnection` | Done | `POST /api/v1/datasources/{id}/test` decrypts secret and delegates to connector stub |
| `TASK-04.2.1-a` `PostgreSqlConnector` | Done | Real JDBC testConnection, discoverColumns, executePreview |
| `TASK-04.2.1-b` `MySqlConnector` | Done | Real JDBC testConnection, discoverColumns, executePreview |
| `TASK-04.2.1-c` `OracleConnector` | Partial | Stub only — no Oracle JDBC driver on Maven Central |
| `TASK-04.2.1-d` `ConnectorFactory` | Done | Factory wired to real adapters; discover/preview exposed via REST |
| `TASK-05.1.1-a` `QueryValidator` | Done | `SimpleReadOnlyQueryValidator` with comment stripping + dangerous pattern blocking |
| `TASK-05.1.1-b` Block DDL, DML, multiple statements | Done | Blocks insert/update/delete/drop/alter/truncate/semicolon after comment stripping |
| `TASK-05.1.1-c` Detect dangerous patterns and comments | Done | Comment stripping (`--` + `/* */`) before validation; 15 dangerous patterns blocked (sleep, benchmark, waitfor, load_file, into outfile/dumpfile, information_schema, pg_catalog, pg_read_file, pg_ls_dir, xp_cmdshell, exec, execute); 26 tests |
| `TASK-05.1.1-d` Extract named parameters | Done | Regex-based extraction implemented |
| `TASK-05.2.1-a` Matrix of valid and invalid cases | Done | 26 tests: accept/reject, comment stripping, 15 dangerous patterns, named param extraction edge cases |
| `TASK-05.2.1-b` Tests by dialect | Done | `SimpleReadOnlyQueryValidatorDialectTest`: 30 tests across PG accept (ILIKE, NULLS LAST, FETCH NEXT, AT TIME ZONE, recursive CTE, JSON operators, `::` cast, ARRAY ANY, qualified schema) + PG reject (pg_sleep — gap fixed, pg_read_file, pg_ls_dir, pg_catalog) + MySQL accept (backtick identifiers, LIMIT/OFFSET, IF, IFNULL, DATE_FORMAT, GROUP_CONCAT, USE INDEX) + MySQL reject (sleep, benchmark, load_file, into outfile) + param-extraction edge cases (:: cast, backtick, recursive CTE). Validator updated: added `pg_sleep(` to DANGEROUS_PATTERNS; `extractNamedParameters` now replaces `::` before regex to prevent spurious params from PG cast syntax. |
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
| `TASK-03.2.1-a` Category CRUD | Done | `CategoryEntity`, `CategoryRepositoryAdapter`, `CategoryApplicationService`, `CategoryController`; name uniqueness checked; `categoryId` FK wired into report create |
| `TASK-08.2.1-a` Resolve dynamic filters | Done | Named param substitution `:name`→`?` with type coercion from `report_parameter.parameter_type` |
| `TASK-08.2.1-b` Execute paginated query | Done | `LIMIT ? OFFSET ?` appended; `DbConnectorPort.executeWithParams` in PG/MySQL adapters |
| `TASK-08.2.1-c` Persist execution history | Done | `report_execution` + `report_execution_parameter` via JPA; COMPLETED/FAILED status + duration |
| `EP-08` Catalog and execution | Done | Catalog listing, ownership ACL, and parameterized execution all done |
| `TASK-09.1.1-a` Request export job | Done | `POST /api/v1/reports/{id}/export` → 202; persists execution+export; fires async |
| `TASK-09.2.1-a` CSV generation | Done | `AsyncExportService` uses `commons-csv:1.11.0` |
| `TASK-09.2.1-b` XLSX generation | Done | `AsyncExportService` uses `poi-ooxml:5.3.0` |
| `TASK-09.2.1-c` Export cleanup job | Done | `ExportCleanupScheduler` `@Scheduled` deletes expired files + DB records |
| `EP-09` Exports | Done | Full async export pipeline + cleanup |
| `TASK-10.1.1-a` Micrometer + Actuator | Done | `MicrometerMetricsAdapter`; execution + export counters/timers/summaries; `/actuator/prometheus` exposed |
| `TASK-10.1.1-b` Prometheus metrics | Done | `micrometer-registry-prometheus` added; endpoint public |
| `TASK-10.1.1-c` Correlation ID + structured logging | Done | `CorrelationIdFilter`; MDC `correlationId`; `X-Correlation-ID` header; MDC propagated to async threads via TaskDecorator |
| `EP-10` Observability and hardening | Done | Observability done; `GlobalExceptionHandler` provides consistent error responses |

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

1. **Observability / performance** (`TASK-10.2.1-a/b/c`): connection pool tuning, capacity baseline, load test scaffolding.

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

# Detailed Implementation Plan

## Plan Objective

Define a detailed work sequence to implement the universal reporting platform with `Java + Spring Boot`, initial `on-premise` deployment, local authentication, multi-DB support, and a target capacity of up to `500` concurrent users.

## Execution Assumptions

- Core team: `1` Tech Lead, `2-3` Java backend developers, `1` frontend developer, `1` shared QA engineer, `1` part-time DevOps engineer.
- Suggested sprint length: `2 weeks`.
- Delivery strategy: functional vertical slices.
- System operational database: `PostgreSQL`.

## Master Implementation Order

1. Technical foundation.
2. Core security and local users.
3. Metadata store and catalogs.
4. Data sources and secure connectivity.
5. SQL validation engine.
6. Column discovery and preview.
7. Report builder.
8. Catalog and execution.
9. Exports and jobs.
10. Observability, performance, and hardening.

## Epic 1: Technical Foundation

### Objective

Establish the project base, internal architecture, conventions, and pipeline.

### Backend Tasks

- Create repository and modular structure:
  - `app-api`
  - `app-domain`
  - `app-infrastructure`
  - `app-connectors`
  - `app-jobs`
- Configure `Spring Boot 3.x`, `Java 21`, or the approved LTS version.
- Configure `local`, `dev`, `qa`, and `prod` profiles.
- Integrate `Flyway` for migrations.
- Configure `SpringDoc/OpenAPI`.
- Configure unified error handling.
- Configure structured logging.
- Define package conventions and ports/adapters.

### Frontend Tasks

- Create the frontend base.
- Define the base layout: login, catalog, builder, administration.
- Configure the HTTP client, error handling, and route guards.

### DevOps Tasks

- Create a CI pipeline:
  - build
  - test
  - static analysis
  - packaging
- Define the deployment artifact as `jar` or container.
- Prepare environment variable templates per environment.

### Exit Criteria

- The project compiles.
- The pipeline runs build and tests.
- Base OpenAPI documentation is published.

## Epic 2: Core Security and Local Users

### Objective

Enable initial authentication with local users and leave the path prepared for evolution to `AD`.

### Backend Tasks

- Implement `user`, `role`, `permission`, and `user_role` entities.
- Implement login with `Spring Security`.
- Implement password hashing with `bcrypt` or an equivalent strong algorithm.
- Implement refresh tokens or controlled sessions.
- Define permissions:
  - `USER_MANAGE`
  - `DATASOURCE_MANAGE`
  - `REPORT_DESIGN`
  - `REPORT_PUBLISH`
  - `REPORT_EXECUTE`
  - `AUDIT_VIEW`
- Create the `AuthenticationProviderPort` interface.
- Implement the `LocalAuthenticationProvider` provider.
- Design a stub or contract for `AdAuthenticationProvider`.
- Implement auditing for login, logout, and user changes.

### Frontend Tasks

- Login screen.
- Local user management.
- Roles and assignment screen.

### Exit Criteria

- Local users can authenticate.
- RBAC works by endpoint and module.
- The domain does not depend directly on `AD`.

## Epic 3: Metadata Store and Base Catalogs

### Objective

Persist report definitions, categories, and auditing.

### Backend Tasks

- Create initial migrations for:
  - `category`
  - `data_source`
  - `report_definition`
  - `report_version`
  - `report_column`
  - `report_parameter`
  - `report_execution`
  - `report_execution_parameter`
  - `report_export`
  - `audit_event`
- Implement repositories and domain services.
- Create CRUD endpoints for categories.
- Implement generic entity auditing.

### Exit Criteria

- Metadata is persisted with basic versioning.
- Change auditing is available.

## Epic 4: Data Sources and Secure Connectivity

### Objective

Register and test connections to Oracle, MySQL, and PostgreSQL.

### Backend Tasks

- Implement secret encryption in the application or integration with the available secret store.
- Create `DataSourceService`.
- Create `ConnectorFactory`.
- Implement `testConnection`.
- Define pooling policies by engine.
- Register datasource technical metadata.
- Implement datasource enable/disable.

### Connector Tasks

- Implement `PostgreSqlConnector`.
- Implement `MySqlConnector`.
- Implement `OracleConnector`.
- Standardize type and error mapping.

### Exit Criteria

- All three engines can be registered and tested.
- Secrets are not stored in plain text.

## Epic 5: SQL Validation Engine

### Objective

Ensure that the input SQL is safe and usable by the system.

### Backend Tasks

- Implement `QueryValidator`.
- Block DDL/DML and multiple statements.
- Implement named parameter extraction.
- Validate suspicious comments and forbidden patterns.
- Add timeout and per-query limits.
- Define functional error codes.

### QA Tasks

- Create a suite of valid and invalid cases.
- Create cases per engine when dialect differences exist.

### Exit Criteria

- The engine rejects unsafe SQL.
- Parameters are discovered correctly.

## Epic 6: Column Discovery and Preview

### Objective

Discover columns and show a controlled preview from the builder.

### Backend Tasks

- Implement `discoverColumns`.
- Implement `executePreview`.
- Define a configurable preview limit.
- Standardize column types into UI types.
- Persist detected column metadata.

### Frontend Tasks

- Show preview grid.
- Show the list of discovered columns.
- Show user-friendly warnings and errors.

### Exit Criteria

- The administrator can visually validate columns and sample data.

## Epic 7: Report Builder

### Objective

Allow creating, editing, versioning, and publishing reports.

### Backend Tasks

- Create `ReportDefinitionService`.
- Implement save draft.
- Implement create new version.
- Implement publish/unpublish.
- Validate that publication is allowed only if preview succeeded.
- Allow column metadata:
  - label
  - order
  - visible
  - format
- Allow parameter configuration:
  - type
  - required
  - default value
  - multi-value

### Frontend Tasks

- Report general metadata editor.
- SQL editor.
- Preview panel.
- Column configuration.
- Filter configuration.
- Publication flow.

### Exit Criteria

- A functional end-to-end builder exists.

## Epic 8: Catalog and Report Execution

### Objective

Expose published reports for end users.

### Backend Tasks

- Implement a filterable report listing.
- Implement configured filter resolution.
- Implement paginated execution.
- Persist `report_execution`.
- Apply ACL by report and datasource.
- Sanitize database errors.

### Frontend Tasks

- Catalog screen.
- Search by name and category.
- Dynamic filter form.
- Paginated results grid.
- Execution history.

### Exit Criteria

- A user can search, open, and execute published reports.

## Epic 9: Exports and Jobs

### Objective

Allow exports without blocking the interactive experience.

### Backend Tasks

- Implement `ExportService`.
- Implement a scheduler or persistent queue.
- Generate CSV.
- Generate XLSX.
- Persist export status.
- Expire and clean temporary files.

### Frontend Tasks

- Request export.
- Visualize job status.
- Download completed file.

### Exit Criteria

- Medium and large exports are processed asynchronously.

## Epic 10: Observability, Performance, and Hardening

### Objective

Prepare the platform for real `on-premise` operation and target load.

### Backend Tasks

- Instrument metrics with `Micrometer`.
- Publish an endpoint for `Prometheus`.
- Add health checks.
- Tune connection pools.
- Tune timeouts and limits.
- Optimize metadata queries.
- Implement rate limiting or quotas if applicable.

### QA/Performance Tasks

- Load tests targeting `500` concurrent users.
- `p95` measurement for critical endpoints.
- Functional UAT with real reports.

### DevOps Tasks

- `on-premise` deployment runbook.
- Backup and recovery runbook.
- Configuration templates for future cloud adoption.

### Exit Criteria

- The platform passes load and basic operational tests.

## Key Dependencies

| Block | Depends on |
|---|---|
| Local security | Technical foundation |
| Metadata store | Technical foundation |
| Data sources | Local security, metadata store |
| SQL validator | Basic data sources |
| Preview | SQL validator, connectors |
| Builder | Preview, metadata store |
| Catalog and execution | Builder, security |
| Exports | Execution |
| Hardening | All previous items |

## Prioritized MVP Backlog

### MVP-1 Foundational

- Local login.
- Basic roles.
- Datasource CRUD.
- Connection test.
- PostgreSQL and MySQL support.

### MVP-2 Reporting Core

- SQL validation.
- Column discovery.
- Preview.
- Builder.
- Report publication.

### MVP-3 Consumption

- Catalog.
- Dynamic filters.
- Paginated execution.
- Auditing.

### MVP-4 Operations

- Oracle connector.
- Exports.
- Observability.
- Performance tuning.

## Planning Risks

| Risk | Impact | Preventive Action |
|---|---|---|
| Oracle delays due to drivers or environment | High | Tackle compatibility in an early sprint |
| SQL validator underestimates complex cases | High | Design the case suite from the start |
| Frontend builder grows too much | Medium | Deliver an incremental, non-WYSIWYG builder |
| Load of 500 concurrent users affects source DB | High | Limits, pagination, async exports, quotas |

## Definition of Ready for Stories

- Clear functional criterion.
- Defined API contract or validated draft.
- Identified security rule.
- Test data available.
- Measurable acceptance criterion.

## Definition of Done for Stories

- Code implemented and reviewed.
- Relevant tests approved.
- Logs and metrics added if applicable.
- Documentation updated.
- No critical vulnerabilities or errors open.

## Final Recommendation

Implementation should start with the shortest vertical path that validates the product: `local login -> PostgreSQL/MySQL datasource -> secure SQL -> preview -> builder -> catalog -> execution`. `Oracle`, advanced exports, and tuning for `500` concurrent users should be addressed very early, but without blocking initial validation of the main flow.

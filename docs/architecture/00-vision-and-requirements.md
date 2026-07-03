# Vision and Requirements

## Problem

Organizations usually keep information distributed across multiple database engines and need to build operational reports without depending on one-off development work for every request. In this case, the focus is an internal platform for exposing specific business information, such as sales, transfers, and other operational processes across the company's systems. The goal is a single platform where:

- an administrator defines a report from a SQL query,
- the system discovers columns and lets users preview the result,
- parameterized filters are published for end users,
- and the user only sees a catalog of available reports to run.

## Product Goals

- Unify SQL report creation and execution across Oracle, MySQL, and PostgreSQL.
- Separate the creation experience from the consumption experience.
- Protect source databases through validation, access control, and operational limits.
- Solve internal operational needs with low implementation and operating cost.
- Allow the platform to evolve toward new engines without redesigning the product.
- Enable report auditing, traceability, and governance.

## Actors

- `Report administrator`: creates, tests, versions, and publishes reports.
- `End user`: runs published reports with allowed filters.
- `Auditor/Support`: reviews executions, errors, timings, and usage.
- `Platform administrator`: manages connections, security, and policies.

## Functional Requirements

### FR-01 Connection Management

- Register Oracle, MySQL, and PostgreSQL connections.
- Store encrypted credentials.
- Test connectivity and basic metadata.
- Associate usage permissions by team, area, or role.

### FR-02 Report Builder

- Create a report with name, description, category, and data source.
- Enter a SQL query.
- Validate that the query is read-only.
- Obtain columns derived from the SQL.
- Show a preview with a configurable row limit.
- Map column metadata such as label, UI type, format, visibility, and order.
- Define filters from safe report parameters.
- Publish, unpublish, and version reports.

### FR-03 Catalog and Execution

- List published reports visible to the user.
- Search by name, category, and tags.
- Request the filters configured for the report.
- Execute the report with pagination.
- Export results to CSV and XLSX.
- Save execution history.

### FR-04 Governance and Auditing

- Record who created, modified, published, and executed each report.
- Record versioned SQL and definition changes.
- Show execution errors without exposing sensitive details.
- Allow compromised reports or connections to be disabled.

### FR-05 Operational Administration

- Configure timeouts, maximum preview rows, and export limits.
- Configure maintenance windows and time-based restrictions.
- Visualize usage and failure metrics.

## Non-Functional Requirements

### NFR-01 Security

- Read-only queries only.
- Encryption of secrets at rest.
- TLS toward services and, when possible, toward source databases.
- RBAC by module, report, and connection.
- Initial authentication with local users managed by the application.
- Design compatible with future `Active Directory` integration.
- Full auditing of changes and executions.
- Error sanitization so schemas, secrets, and sensitive SQL are never exposed.

### NFR-02 Performance

- Preview in less than 5 seconds for medium-sized queries over bounded datasets.
- Server-side pagination for interactive results.
- Heavy exports handled through asynchronous processes.
- Configurable limits per query and per user.

### NFR-03 Scalability

- Horizontal scaling of the API and workers.
- Ability to add new connectors without modifying the core domain.
- Initial support for up to 500 concurrent users, with later growth without major redesign.

### NFR-04 Maintainability

- Clear separation among domain, connectors, security, and UI.
- Stable API contracts.
- ADRs and decision traceability.
- Automated coverage for SQL validation, filters, and connectors.

### NFR-05 Availability and Support

- Controlled retries for transient failures.
- Correlated traces per execution.
- Observability dashboard with latency and error-rate metrics.

### NFR-06 Deployment and Portability

- Initial `on-premise` deployment.
- Packaging and configuration ready for future cloud migration.
- Minimized infrastructure dependencies to reduce initial cost.
- Preference for open source components or technologies already standardized in the company.

## First Version Scope

- Engines: Oracle, MySQL, PostgreSQL.
- Report types: tabular.
- Filters: text, numeric, date, list, range, boolean.
- Exports: CSV and XLSX.
- Interactive execution and asynchronous execution for large exports.
- Local authentication with internal user and role management.

## Out of Initial Scope

- ETL or data replication.
- Complex visual dashboard-style reports.
- Write SQL or stored procedures with side effects.
- External multi-tenant support with strict regulatory isolation.
- Advanced WYSIWYG layout designer.

## Architecture Acceptance Criteria

- Adding a new engine must require creating a connector without rewriting the domain.
- No end user can freely edit SQL when running a report.
- Every published report must have a validated preview and consistent column metadata.
- Every execution must be audited with user, time, filters, and outcome.
- The system must sustain a target load of up to 500 concurrent users with controlled degradation.

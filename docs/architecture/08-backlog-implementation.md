# Implementation Backlog

## Objective

Translate the architecture and the detailed plan into an operational backlog ready to be managed in `Jira`, `Azure DevOps`, or an equivalent tool.

## Suggested Conventions

- `EP`: epic
- `FEAT`: feature
- `US`: user story
- `TASK`: technical task
- `SPIKE`: time-boxed investigation
- Priorities: `P1`, `P2`, `P3`

## Release View

| Release | Objective |
| --- | --- |
| `R1 - Base MVP` | Local security, data sources, secure SQL, preview, and basic builder |
| `R2 - Consumption` | Catalog, execution, auditing, and exports |
| `R3 - Hardening` | Robust Oracle support, performance, observability, and operational readiness |
| `R4 - Evolution` | `AD` integration, UX improvements, and functional expansion |

## EP-01 Technical Foundation

### FEAT-01.1 Repository and Multi-Module Build

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-01.1.1` | User Story | As a developer, I want a multi-module structure to separate domain, application, and infrastructure | P1 | - |
| `TASK-01.1.1-a` | Task | Create root `settings.gradle`, `build.gradle`, and base modules | P1 | - |
| `TASK-01.1.1-b` | Task | Configure Java versioning and Spring Boot BOM | P1 | `TASK-01.1.1-a` |
| `TASK-01.1.1-c` | Task | Configure build and test plugins | P1 | `TASK-01.1.1-a` |

### FEAT-01.2 Environments and Configuration

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-01.2.1` | User Story | As an operator, I want environment profiles so I can deploy without changing code | P1 | `US-01.1.1` |
| `TASK-01.2.1-a` | Task | Create base `application.yml` and `local`, `dev`, `qa`, `prod` profiles | P1 | `US-01.1.1` |
| `TASK-01.2.1-b` | Task | Externalize sensitive variables | P1 | `TASK-01.2.1-a` |

### FEAT-01.3 Quality and Pipeline

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-01.3.1` | User Story | As a team, we want a CI pipeline to validate build and tests on every change | P1 | `US-01.1.1` |
| `TASK-01.3.1-a` | Task | Define a build, test, and static analysis pipeline | P1 | `US-01.1.1` |
| `TASK-01.3.1-b` | Task | Publish an executable artifact | P2 | `TASK-01.3.1-a` |

## EP-02 Security and Local Users

### FEAT-02.1 Local Login

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-02.1.1` | User Story | As a user, I want to sign in with local credentials to access the platform | P1 | `EP-01` |
| `TASK-02.1.1-a` | Task | Create user, role, and permission entities | P1 | `EP-01` |
| `TASK-02.1.1-b` | Task | Configure `Spring Security` and password hashing | P1 | `TASK-02.1.1-a` |
| `TASK-02.1.1-c` | Task | Implement the authentication endpoint | P1 | `TASK-02.1.1-b` |

### FEAT-02.2 RBAC Authorization

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-02.2.1` | User Story | As an administrator, I want to control permissions by module to protect sensitive functionality | P1 | `US-02.1.1` |
| `TASK-02.2.1-a` | Task | Define the initial permissions matrix | P1 | `US-02.1.1` |
| `TASK-02.2.1-b` | Task | Apply authorization by endpoint | P1 | `TASK-02.2.1-a` |
| `TASK-02.2.1-c` | Task | Apply permissions by report and datasource | P2 | `TASK-02.2.1-b` |

### FEAT-02.3 Evolution to AD

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `SPIKE-02.3.1` | Spike | Evaluate the `LDAP/AD` strategy for a later phase | P3 | `US-02.1.1` |
| `TASK-02.3.1-a` | Task | Design a decoupled authentication port | P1 | `US-02.1.1` |

## EP-03 Metadata Store and Catalogs

### FEAT-03.1 Operational Model

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-03.1.1` | User Story | As a system, I want to persist reports, versions, and auditing to support governance | P1 | `EP-01` |
| `TASK-03.1.1-a` | Task | Create initial migrations with `Flyway` | P1 | `EP-01` |
| `TASK-03.1.1-b` | Task | Implement repositories and base services | P1 | `TASK-03.1.1-a` |
| `TASK-03.1.1-c` | Task | Record entity auditing | P2 | `TASK-03.1.1-b` |

### FEAT-03.2 Categories and Classification

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-03.2.1` | User Story | As an administrator, I want to organize reports by categories and tags | P2 | `US-03.1.1` |
| `TASK-03.2.1-a` | Task | Category CRUD | P2 | `US-03.1.1` |
| `TASK-03.2.1-b` | Task | Tag and ownership model | P3 | `US-03.1.1` |

## EP-04 Data Sources and Connectivity

### FEAT-04.1 Datasource Management

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-04.1.1` | User Story | As an administrator, I want to register Oracle, MySQL, and PostgreSQL connections so I can use them in reports | P1 | `EP-02`, `EP-03` |
| `TASK-04.1.1-a` | Task | Implement `data_source` CRUD | P1 | `EP-03` |
| `TASK-04.1.1-b` | Task | Implement secret encryption | P1 | `TASK-04.1.1-a` |
| `TASK-04.1.1-c` | Task | Implement `testConnection` | P1 | `TASK-04.1.1-a` |

### FEAT-04.2 Multi-DB Connectors

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-04.2.1` | User Story | As a system, I want to abstract engines so reports can run without coupling the domain to a dialect | P1 | `US-04.1.1` |
| `TASK-04.2.1-a` | Task | Implement `PostgreSqlConnector` | P1 | `US-04.1.1` |
| `TASK-04.2.1-b` | Task | Implement `MySqlConnector` | P1 | `US-04.1.1` |
| `TASK-04.2.1-c` | Task | Implement `OracleConnector` | P2 | `US-04.1.1` |
| `TASK-04.2.1-d` | Task | Implement `ConnectorFactory` | P1 | `TASK-04.2.1-a`, `TASK-04.2.1-b` |

## EP-05 Secure SQL

### FEAT-05.1 Validation and Parser

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-05.1.1` | User Story | As an administrator, I want my SQL to be validated as read-only before saving a report | P1 | `EP-04` |
| `TASK-05.1.1-a` | Task | Implement `QueryValidator` | P1 | `EP-04` |
| `TASK-05.1.1-b` | Task | Block DDL, DML, and multiple statements | P1 | `TASK-05.1.1-a` |
| `TASK-05.1.1-c` | Task | Detect dangerous patterns and comments | P1 | `TASK-05.1.1-a` |
| `TASK-05.1.1-d` | Task | Extract named parameters | P1 | `TASK-05.1.1-a` |

### FEAT-05.2 Validator Testing

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-05.2.1` | User Story | As a team, we want automated validator tests to prevent regressions | P1 | `US-05.1.1` |
| `TASK-05.2.1-a` | Task | Create a matrix of valid and invalid cases | P1 | `US-05.1.1` |
| `TASK-05.2.1-b` | Task | Add tests by dialect if applicable | P2 | `TASK-05.2.1-a` |

## EP-06 Preview and Discovery

### FEAT-06.1 Column Discovery

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-06.1.1` | User Story | As an administrator, I want to discover columns automatically from SQL to configure the report | P1 | `EP-05` |
| `TASK-06.1.1-a` | Task | Implement `discoverColumns` | P1 | `EP-05` |
| `TASK-06.1.1-b` | Task | Standardize output types | P1 | `TASK-06.1.1-a` |

### FEAT-06.2 Controlled Preview

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-06.2.1` | User Story | As an administrator, I want to see a limited result preview before publishing the report | P1 | `US-06.1.1` |
| `TASK-06.2.1-a` | Task | Implement `executePreview` with limits | P1 | `US-06.1.1` |
| `TASK-06.2.1-b` | Task | Expose the preview endpoint | P1 | `TASK-06.2.1-a` |

## EP-07 Report Builder

### FEAT-07.1 Definition and Versioning

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-07.1.1` | User Story | As an administrator, I want to create and version reports to maintain change control | P1 | `EP-06` |
| `TASK-07.1.1-a` | Task | Implement `ReportDefinitionService` | P1 | `EP-06` |
| `TASK-07.1.1-b` | Task | Save draft and create new version | P1 | `TASK-07.1.1-a` |
| `TASK-07.1.1-c` | Task | Publish and unpublish reports | P1 | `TASK-07.1.1-b` |

### FEAT-07.2 Column and Filter Configuration

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-07.2.1` | User Story | As an administrator, I want to define labels, visibility, and filters to control the final experience | P1 | `US-07.1.1` |
| `TASK-07.2.1-a` | Task | Configure visible columns, order, and format | P1 | `US-07.1.1` |
| `TASK-07.2.1-b` | Task | Configure parameters, defaults, and required flags | P1 | `US-07.1.1` |
| `TASK-07.2.1-c` | Task | Validate that publication only happens after a successful preview | P1 | `US-07.1.1` |

## EP-08 Catalog and Execution

### FEAT-08.1 End-User Catalog

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-08.1.1` | User Story | As an end user, I want to see a catalog of published reports so I can run only authorized ones | P1 | `EP-07`, `EP-02` |
| `TASK-08.1.1-a` | Task | Implement a filterable listing | P1 | `EP-07` |
| `TASK-08.1.1-b` | Task | Apply permissions by report | P1 | `US-08.1.1` |

### FEAT-08.2 Paginated Execution

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-08.2.1` | User Story | As an end user, I want to execute reports with filters to get relevant information | P1 | `US-08.1.1` |
| `TASK-08.2.1-a` | Task | Resolve dynamic filters | P1 | `US-08.1.1` |
| `TASK-08.2.1-b` | Task | Execute paginated query | P1 | `TASK-08.2.1-a` |
| `TASK-08.2.1-c` | Task | Persist execution history | P1 | `TASK-08.2.1-b` |

## EP-09 Exports

### FEAT-09.1 Asynchronous Jobs

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-09.1.1` | User Story | As an end user, I want to export results without blocking the interface | P2 | `EP-08` |
| `TASK-09.1.1-a` | Task | Implement a persistent queue or job scheduler | P2 | `EP-08` |
| `TASK-09.1.1-b` | Task | Persist job status | P2 | `TASK-09.1.1-a` |

### FEAT-09.2 Export Formats

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-09.2.1` | User Story | As an end user, I want CSV and XLSX exports so I can consume data outside the platform | P2 | `US-09.1.1` |
| `TASK-09.2.1-a` | Task | Generate CSV | P2 | `US-09.1.1` |
| `TASK-09.2.1-b` | Task | Generate XLSX | P2 | `US-09.1.1` |
| `TASK-09.2.1-c` | Task | Expire and clean files | P2 | `TASK-09.2.1-a`, `TASK-09.2.1-b` |

## EP-10 Observability and Hardening

### FEAT-10.1 Observability

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-10.1.1` | User Story | As an operator, I want metrics and health checks to monitor the platform | P2 | `EP-08` |
| `TASK-10.1.1-a` | Task | Integrate `Micrometer` and `Actuator` | P2 | `EP-08` |
| `TASK-10.1.1-b` | Task | Publish metrics for `Prometheus` | P2 | `TASK-10.1.1-a` |
| `TASK-10.1.1-c` | Task | Add correlation id and structured logging | P2 | `EP-01` |

### FEAT-10.2 Performance and Capacity

| ID | Type | Description | Priority | Dependencies |
| --- | --- | --- | --- | --- |
| `US-10.2.1` | User Story | As a business stakeholder, I want to support up to 500 concurrent users with controlled degradation | P2 | `EP-08`, `EP-09` |
| `TASK-10.2.1-a` | Task | Run load tests | P2 | `EP-08` |
| `TASK-10.2.1-b` | Task | Tune pools, timeouts, and limits | P2 | `TASK-10.2.1-a` |
| `TASK-10.2.1-c` | Task | Define quotas or rate limiting if applicable | P3 | `TASK-10.2.1-b` |

## Suggested Sprint Prioritization

| Sprint | Objective | Main Items |
| --- | --- | --- |
| `Sprint 1` | Foundation | `EP-01`, base of `EP-02` |
| `Sprint 2` | Security and metadata | close `EP-02`, base of `EP-03` |
| `Sprint 3` | Data sources | `EP-04` PostgreSQL/MySQL |
| `Sprint 4` | Secure SQL | `EP-05` |
| `Sprint 5` | Preview | `EP-06` |
| `Sprint 6` | Builder | `EP-07` |
| `Sprint 7` | Catalog and execution | `EP-08` |
| `Sprint 8` | Exports and initial hardening | `EP-09`, start of `EP-10` |
| `Sprint 9` | Robust Oracle and performance | close Oracle in `EP-04` and `EP-10` |

## Critical MVP Stories

- `US-02.1.1`: local login.
- `US-04.1.1`: datasource registration.
- `US-04.2.1`: PostgreSQL/MySQL connectors.
- `US-05.1.1`: secure SQL validation.
- `US-06.2.1`: controlled preview.
- `US-07.1.1`: versioning and publication.
- `US-08.2.1`: execution with filters.

## Final Recommendation

This backlog should be used as the team's planning source. The best practice is to create the epics and features first in the management tool, then load the critical MVP user stories, and finally derive the technical tasks by sprint without overloading the board with items from very distant phases.


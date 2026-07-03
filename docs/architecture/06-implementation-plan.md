# Implementation Plan

## Phase 0: Definition

- Confirm `Spring Boot` as the base stack.
- Confirm initial `on-premise` deployment and `cloud-ready` guidelines.
- Confirm local authentication for the MVP and a later `AD` integration strategy.
- Validate security and compliance policies.

## Phase 1: Foundation Platform

- Create the modular backend structure.
- Implement base authentication, authorization, and auditing.
- Implement the metadata store.
- Create the connections and secrets module.

## Phase 2: SQL Engine

- Implement `QueryValidator`.
- Implement `Connector Factory`.
- Implement connectors for PostgreSQL and MySQL.
- Implement the Oracle connector.
- Implement column discovery and preview.

## Phase 3: Report Builder

- UI to create reports and select a datasource.
- SQL editor with validation and preview.
- Column and filter configuration.
- Versioning and publication.

## Phase 4: Catalog and Execution

- Published report listing.
- Dynamic filter form.
- Paginated execution.
- Execution history.

## Phase 5: Exports and Operations

- Job queue and workers.
- CSV/XLSX exports.
- Monitoring, metrics, and alerts.
- Automated cleanup of temporary files.

## Suggested Sprint Deliverables

| Sprint | Deliverable |
|---|---|
| 1 | Spring Boot structure, local auth, metadata store |
| 2 | Data sources, secret encryption, and connection test |
| 3 | Validator and PostgreSQL/MySQL connectors |
| 4 | Oracle connector, preview, and discovery |
| 5 | Report builder, versioning, and publication |
| 6 | Catalog, filters, execution, and auditing |
| 7 | Exports, observability, and tuning for 500 concurrent users |
| 8 | Hardening, UAT, and `on-premise` deployment readiness |

## Suggested Team

- 1 Tech Lead / Architect.
- 2 or 3 Java backend developers.
- 1 Frontend developer.
- 1 QA automation engineer or shared SDET.
- 1 part-time DevOps engineer if the environment does not yet exist.

## Technical Definition of Done

- Feature with relevant automated tests.
- Logs and metrics added.
- Security reviewed for secrets handling and SQL.
- Technical documentation and runbooks updated.

## Final Recommendation

Implement by vertical phases rather than isolated layers. The first value path should cover end-to-end: register connection, create a simple report, preview it, and execute it in the catalog, all on the final `Java + Spring Boot` foundation.

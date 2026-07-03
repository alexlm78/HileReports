# Security, Operations, and NFRs

## Security

### Identity and Access

- Implement local authentication in the first version.
- Design a replaceable `AuthenticationProvider` to integrate `LDAP/AD` in a later phase.
- Apply RBAC by role and ACL by report/connection.
- Separate permissions to create, publish, execute, and audit.

### Secret Handling

- Store source database credentials in a secret manager or with strong encryption.
- Never persist passwords in plain text.
- Support secret rotation without republishing reports.

### SQL Security

- Parser or validator to allow read-only queries only.
- Block multiple statements.
- Mandatory use of typed parameters.
- Maintain a list of blocked functions or patterns if they affect security or performance.
- Timeout per query and maximum row count by context.

### Data Security

- Mask sensitive parameters in audit logs.
- Do not store complete datasets except for temporary exports.
- Expire exported files.
- Minimize logs containing personal or financial data.

## Performance

### Strategies

- Limited preview, for example 50 or 100 rows.
- Mandatory server-side pagination.
- Cache column metadata and report definitions.
- Connection pools by engine and by datasource.
- Circuit breakers or throttling for slow sources.

### Initial Budgets

| Scenario | Target |
|---|---|
| Report listing | < 1 s |
| Builder preview | < 5 s |
| Interactive paginated execution | < 8 s target p95 |
| Target concurrency | up to 500 concurrent users |
| Large export | asynchronous |

## Availability and Resilience

- Stateless API deployable in multiple `on-premise` instances.
- Horizontally scalable workers.
- Retries only for idempotent transient failures.
- Disable one datasource without affecting the entire system.
- Durable queue for exports.
- Externalized configuration to enable later cloud migration without major code changes.

## Observability

### Logs

- Every request and execution must have a `correlation_id`.
- Record datasource, report, duration, result, and error type.

### Metrics

- SQL validation time.
- Preview time.
- Execution time by engine.
- Number of executions per report.
- Failure rate by datasource.
- Pending exports and average duration.

### Traces

- Trace end-to-end from UI/API to the Query Engine and worker.

## Costs

- Starting with a modular monolith avoids high platform costs.
- The largest costs will be in enterprise connectivity, observability, and heavy exports.
- To keep the budget low, avoid non-essential commercial dependencies in the first phase.
- If high export concurrency is required, isolating workers reduces cost on the main node.

## Maintainability

- Hexagonal or ports-and-adapters architecture in the backend.
- Isolated modules by bounded context.
- Automated tests for validation and connectors.
- Feature flags for new engines or capabilities.

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Unsafe or expensive SQL | High | Parser, allowlist, timeout, limits, publication review |
| Dialect differences | Medium-High | Connectors by engine and compatibility testing |
| Excess load on source DB | High | Limited preview, pagination, time windows, rate limit |
| Compromised credentials | High | Secret manager, rotation, auditing, and least privilege |
| Huge exports | Medium | Async jobs, quotas, and expiration |

## Testing Strategy

- Unit tests for SQL validation and parameter extraction.
- Contract tests per `DbConnector`.
- Integration tests against MySQL/PostgreSQL containers.
- Controlled integration tests for Oracle depending on licensing and environment.
- E2E tests for builder, preview, and report execution.

## Final Recommendation

The first version should invest early in SQL security, robust local authentication, observability, and connector testing. These are the areas with the highest technical and operational risk for an internal `on-premise` platform targeting up to `500` concurrent users.

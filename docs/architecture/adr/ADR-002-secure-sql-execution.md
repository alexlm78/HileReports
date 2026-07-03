# ADR-002: Validation and Safe SQL Execution

## Status

Proposed approval

## Context

The system will allow administrators to define reports from user-provided SQL. This introduces security risk, performance impact, and potential effects on source databases.

## Decision

Allow read-only queries exclusively (`SELECT` and `WITH`), with prior validation, named parameter extraction, blocking of multiple statements, and execution with row/timeout limits.

## Consequences

### Positive

- Reduces the risk of accidental or malicious modification.
- Makes it feasible to expose a controlled builder.
- Improves observability and governance.

### Negative

- Some complex queries will not be supported initially.
- Requires investment in parser/validator work and testing.

## Follow-Up

- Define the catalog of allowed and blocked patterns.
- Measure validator false positives during UAT.

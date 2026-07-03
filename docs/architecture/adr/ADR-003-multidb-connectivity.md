# ADR-003: Multi-DB Connectivity Abstraction with Connectors

## Status

Proposed approval

## Context

The product must query Oracle, MySQL, and PostgreSQL, and potentially other engines in the future. Differences in drivers, dialects, and metadata must not leak into the domain layer.

## Decision

Implement a `ConnectorFactory` with dedicated connectors per engine. Each connector will fulfill a shared contract for connection testing, column discovery, preview, and execution.

## Consequences

### Positive

- Isolates dialect differences.
- Allows new engines to be added with low impact.
- Facilitates contract testing.

### Negative

- Requires maintaining adapters and compatibility matrices.
- Some advanced engine capabilities will not be exposed immediately.

## Follow-Up

- Publish an implementation guide for new connectors.
- Maintain a suite of contract tests per engine.

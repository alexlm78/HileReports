# ADR-004: Java with Spring Boot as the Base Stack

## Status

Proposed approval

## Context

The system must support Oracle, MySQL, and PostgreSQL, operate initially `on-premise`, keep cost low, and provide a clear path for future evolution. The team's preferred stack is `Java`, with a choice between `Spring` and `Quarkus`.

## Decision

Adopt **Java + Spring Boot** as the base stack for the first version.

## Consequences

### Positive

- Excellent maturity for enterprise connectivity.
- Natural integration with `Spring Security`, observability, and scheduling.
- Lower implementation risk for a product with several cross-cutting concerns.
- Easy support for portable packaging and `on-premise` deployment.

### Negative

- Higher resource usage and verbosity than some alternatives.
- Slower startup times compared with `Quarkus`.

## Follow-Up

- Revisit `Quarkus` if footprint or startup times become a priority in a future phase.

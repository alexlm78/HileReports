# ADR-001: Modular Monolith as the Base Architecture

## Status

Proposed approval

## Context

A universal reporting platform is required with a builder, preview, execution catalog, multi-DB connectivity, and security controls over SQL. The initial scope does not justify the operational complexity of microservices.

## Decision

Adopt a **modular monolith** with clear internal boundaries, a stateless API, and asynchronous workers for heavy tasks.

## Consequences

### Positive

- Faster delivery.
- Lower operational cost.
- Greater simplicity for domain evolution.

### Negative

- Lower fault isolation.
- Requires discipline to avoid coupling between modules.

## Follow-Up

- Revisit this decision if the team grows beyond several squads or if execution/export load requires independent isolation.

# ADR-005: Initial Local Authentication with Evolution to Active Directory

## Status

Proposed approval

## Context

The first version must ship with low cost and controlled dependencies, but the medium-term goal is integration with the corporate network identity.

## Decision

Implement local authentication in the first version, encapsulated behind an authentication port, leaving future integration with `LDAP/Active Directory` prepared.

## Consequences

### Positive

- Reduces external dependencies for the MVP.
- Allows progress even if corporate integration is not ready.
- Avoids coupling domain security to a specific provider from the start.

### Negative

- Users will need to be migrated or federated in a later phase.
- The team will temporarily maintain local user administration.

## Follow-Up

- Design user identifiers and roles from the start to ease the transition to `AD`.
- Add integration tests with `LDAP/AD` in the later phase.

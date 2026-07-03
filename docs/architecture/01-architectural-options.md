# Architectural Options

## Decision Context

The system needs to:

- support multiple database engines,
- execute read-only SQL safely,
- provide a builder with preview capabilities,
- and scale reasonably without unnecessary complexity.

## Option 1: Modular Monolith with Workers

A single application with clearly bounded internal modules: authentication, catalog, report design, execution, connectors, auditing, and exports. Heavy workloads are delegated to asynchronous workers.

### Option 1 Pros

- Lower operational and deployment complexity.
- Faster time to market.
- Simpler transactions and metadata consistency.
- Easier to govern for a small or medium-sized team.
- Can evolve module by module into independent services.

### Option 1 Cons

- Less fault isolation than a microservices architecture.
- Codebase growth requires architectural discipline.
- Fine-grained per-component scaling is more limited.

### When to Choose Option 1

- Teams of 4 to 12 developers.
- Need to ship quickly with controlled risk.
- Domain still evolving.

## Option 2: Microservices by Capability

Separate services for catalog, report builder, execution, connectivity, and exports, integrated through APIs and events.

### Option 2 Pros

- Independent scaling by capability.
- Better fault isolation.
- Large teams can work with greater autonomy.
- Easier to isolate execution and export-heavy workloads.

### Option 2 Cons

- Much higher operational complexity.
- Requires mature DevOps, observability, and governance.
- Higher initial development cost.
- Metadata consistency and versioning become more complex.

### When to Choose Option 2

- High scale from day one.
- Multiple teams with clear domain ownership.
- Strong isolation and resilience requirements.

## Option 3: Monolithic Backend with a Decoupled Query Engine

The UI and metadata management live in a central application, while SQL validation and execution are encapsulated in a separate engine with its own API.

### Option 3 Pros

- Good balance between simplicity and isolation of the most sensitive component.
- Allows the engine to evolve into a specialized service later.
- Makes it easier to reinforce security controls in the engine.

### Option 3 Cons

- Increases complexity compared to a pure monolith.
- Duplicates part of the operational concerns.
- Can be premature if the initial volume is moderate.

### When to Choose Option 3

- High perceived risk in the SQL execution subsystem.
- Need to isolate networks or technical credentials.

## Comparison

| Criterion | Option 1 | Option 2 | Option 3 |
| --- | --- | --- | --- |
| Delivery speed | High | Medium-Low | Medium |
| Operational complexity | Low | High | Medium |
| Independent scaling | Medium | High | High in the engine |
| Initial cost | Low | High | Medium |
| SQL engine security | Medium-High | High | High |
| Initial maintainability | High | Medium | Medium-High |
| Fit for current scope | Very high | Low-Medium | High |

## Final Recommendation

The recommended choice is **Option 1: modular monolith with asynchronous workers**, with a strong internal boundary around the SQL validation and execution engine. This architecture offers the best balance of simplicity, security, cost, and maintainability for a first enterprise release with multi-DB support.

The recommendation includes two principles to avoid future lock-in:

- Define explicit interfaces for `QueryValidator`, `QueryExecutor`, `DbConnector`, and `ReportPublisher`.
- Keep the execution subsystem logically decoupled so it can be extracted later if load, risk, or team structure justifies it.

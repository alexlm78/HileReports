# Hile Reports

Universal multi-DB reporting platform. Full-stack MVP: modular Java backend + React admin/consumption frontend.

## Prerequisites

| Tool | Minimum version |
|---|---|
| Java | 21 |
| Docker + Docker Compose | any recent version |
| Node.js | 18+ |
| npm | 9+ |

## Architecture

Modular monolith. Dependency direction: `bootstrap → application → domain`.

| Module | Role |
|---|---|
| `reporting-domain` | Core records and enums — no Spring, no framework deps |
| `reporting-application` | Use cases, DTOs, outbound ports — no Spring |
| `reporting-infrastructure` | JPA adapters, Flyway migrations |
| `reporting-connectors` | Real JDBC adapters: PostgreSQL, MySQL, Oracle |
| `reporting-security` | Local auth provider, JWT, AD stub |
| `reporting-jobs` | Async export cleanup scheduler |
| `reporting-bootstrap` | Spring Boot app, REST controllers, wiring |

## Running the full stack

### Step 1 — Start PostgreSQL

```bash
# From project root (where compose.yaml lives)
docker compose up -d

# Verify healthy
docker compose ps
```

Default credentials: host `localhost:5432`, database `hile_reports`, user `postgres`, password `postgres`.

### Step 2 — Start the backend

```bash
# From project root
./gradlew bootRun --project-dir reporting-bootstrap
```

Flyway runs automatically on boot — applies all migrations (V1–V5) and seeds the default admin user.

Verify the backend is up:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

To test login, use Swagger UI at `http://localhost:8080/swagger-ui.html` → `POST /api/v1/auth/login` with body `{"username":"admin","password":"admin123"}`.

### Step 3 — Start the frontend

In a separate terminal:

```bash
cd reporting-frontend
npm install        # first time only
npm run dev
```

Frontend available at `http://localhost:3000`. All `/api/*` requests are proxied to `http://localhost:8080`.

### Quick start (three terminals)

```bash
# Terminal 1
docker compose up -d

# Terminal 2 (wait for DB healthy)
./gradlew bootRun --project-dir reporting-bootstrap

# Terminal 3 (wait for backend ready)
cd reporting-frontend && npm install && npm run dev
```

## Default credentials

```
username: admin
password: admin123
```

**Change immediately in any non-local environment.**

## URLs

| URL | Description |
|---|---|
| `http://localhost:3000` | React frontend |
| `http://localhost:3000/catalog` | Published reports catalog |
| `http://localhost:3000/admin` | Admin panel (PLATFORM_ADMIN only) |
| `http://localhost:8080/swagger-ui.html` | Swagger UI — all REST endpoints |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON spec |
| `http://localhost:8080/actuator/health` | Health check |
| `http://localhost:8080/actuator/prometheus` | Prometheus metrics |

## Environment variables

| Variable | Default | Notes |
|---|---|---|
| `APP_DB_URL` | `jdbc:postgresql://localhost:5432/hile_reports` | |
| `APP_DB_USERNAME` | `postgres` | |
| `APP_DB_PASSWORD` | `postgres` | |
| `APP_SERVER_PORT` | `8080` | |
| `APP_SECURITY_MODE` | `local` | |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:4200` | |
| `APP_JWT_SECRET` | dev key | **Min 32 bytes — change in production** |
| `APP_ENCRYPTION_SECRET` | dev key | **AES-256-GCM source — change in production** |
| `APP_DB_SCHEMA` | `hile_reports` | Used by Flyway and Hibernate |

## Backend commands

```bash
# Build all modules
./gradlew build -x test

# Run all tests
./gradlew test

# Run tests for a single module
./gradlew :reporting-infrastructure:test

# Apply code formatting (required before committing)
./gradlew spotlessApply

# Verify formatting + tests (CI gate)
./gradlew spotlessCheck test
```

## Frontend commands

```bash
cd reporting-frontend

# Development server (port 3000, proxies /api to localhost:8080)
npm run dev

# Production build
npm run build

# Type check only
npx tsc --noEmit
```

## Documentation

- `docs/architecture/10-implementation-memory.md` — verified implementation state, backlog alignment
- `docs/architecture/08-backlog-implementation.md` — full feature backlog
- `CLAUDE.md` — AI agent guidance and active endpoint reference

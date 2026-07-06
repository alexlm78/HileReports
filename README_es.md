# Hile Reports

Plataforma universal de reportes multi-DB. MVP full-stack: backend Java modular + frontend React de administración y consumo.

## Prerequisitos

| Herramienta | Versión mínima |
|---|---|
| Java | 21 |
| Docker + Docker Compose | cualquier versión reciente |
| Node.js | 18+ |
| npm | 9+ |

## Arquitectura

Monolito modular. Dirección de dependencias: `bootstrap → application → domain`.

| Módulo | Rol |
|---|---|
| `reporting-domain` | Registros y enums del dominio — sin Spring, sin dependencias de framework |
| `reporting-application` | Casos de uso, DTOs, puertos de salida — sin Spring |
| `reporting-infrastructure` | Adaptadores JPA, migraciones Flyway |
| `reporting-connectors` | Adaptadores JDBC reales: PostgreSQL, MySQL, Oracle |
| `reporting-security` | Proveedor de autenticación local, JWT, stub AD |
| `reporting-jobs` | Scheduler de limpieza de exportaciones async |
| `reporting-bootstrap` | App Spring Boot, controladores REST, cableado |

## Ejecutar el stack completo

### Paso 1 — Iniciar PostgreSQL

```bash
# Desde la raíz del proyecto (donde está compose.yaml)
docker compose up -d

# Verificar que está healthy
docker compose ps
```

Credenciales por defecto: host `localhost:5432`, base de datos `hile_reports`, usuario `postgres`, contraseña `postgres`.

### Paso 2 — Iniciar el backend

```bash
# Desde la raíz del proyecto
./gradlew bootRun --project-dir reporting-bootstrap
```

Flyway corre automáticamente al arrancar — aplica todas las migraciones (V1–V5) y crea el usuario admin por defecto.

Verificar que el backend está levantado:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

Para probar el login, usar Swagger UI en `http://localhost:8080/swagger-ui.html` → `POST /api/v1/auth/login` con body `{"username":"admin","password":"admin123"}`.

### Paso 3 — Iniciar el frontend

En una terminal separada:

```bash
cd reporting-frontend
npm install        # solo la primera vez
npm run dev
```

Frontend disponible en `http://localhost:3000`. Todas las peticiones `/api/*` se redirigen automáticamente a `http://localhost:8080`.

### Inicio rápido (tres terminales)

```bash
# Terminal 1
docker compose up -d

# Terminal 2 (esperar DB healthy)
./gradlew bootRun --project-dir reporting-bootstrap

# Terminal 3 (esperar backend listo)
cd reporting-frontend && npm install && npm run dev
```

## Credenciales por defecto

```
usuario: admin
contraseña: admin123
```

**Cambiar inmediatamente en cualquier entorno non-local.**

## URLs

| URL | Descripción |
|---|---|
| `http://localhost:3000` | Frontend React |
| `http://localhost:3000/catalog` | Catálogo de reportes publicados |
| `http://localhost:3000/admin` | Panel de administración (solo PLATFORM_ADMIN) |
| `http://localhost:8080/swagger-ui.html` | Swagger UI — todos los endpoints REST |
| `http://localhost:8080/v3/api-docs` | Especificación OpenAPI JSON |
| `http://localhost:8080/actuator/health` | Health check |
| `http://localhost:8080/actuator/prometheus` | Métricas Prometheus |

## Variables de entorno

| Variable | Por defecto | Notas |
|---|---|---|
| `APP_DB_URL` | `jdbc:postgresql://localhost:5432/hile_reports` | |
| `APP_DB_USERNAME` | `postgres` | |
| `APP_DB_PASSWORD` | `postgres` | |
| `APP_SERVER_PORT` | `8080` | |
| `APP_SECURITY_MODE` | `local` | |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:4200` | |
| `APP_JWT_SECRET` | dev key | **Mín. 32 bytes — cambiar en producción** |
| `APP_ENCRYPTION_SECRET` | dev key | **Fuente AES-256-GCM — cambiar en producción** |
| `APP_DB_SCHEMA` | `hile_reports` | Usado por Flyway e Hibernate |

## Comandos backend

```bash
# Compilar todos los módulos
./gradlew build -x test

# Ejecutar todos los tests
./gradlew test

# Ejecutar tests de un módulo específico
./gradlew :reporting-infrastructure:test

# Aplicar formato de código (obligatorio antes de commitear)
./gradlew spotlessApply

# Verificar formato + tests (gate de CI)
./gradlew spotlessCheck test
```

## Comandos frontend

```bash
cd reporting-frontend

# Servidor de desarrollo (puerto 3000, proxy /api → localhost:8080)
npm run dev

# Build de producción
npm run build

# Solo verificación de tipos TypeScript
npx tsc --noEmit
```

## Documentación

- `docs/architecture/10-implementation-memory.md` — estado verificado de implementación, alineación con backlog
- `docs/architecture/08-backlog-implementation.md` — backlog completo de funcionalidades
- `CLAUDE.md` — guía para agentes IA y referencia de endpoints activos

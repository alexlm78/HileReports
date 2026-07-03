# API y Flujos

## Principios API

- API REST para operaciones de negocio.
- Respuestas paginadas para listados y resultados.
- Idempotencia para operaciones de prueba donde aplique.
- Versionado de API via `/api/v1`.
- Autorizacion por scopes y permisos de dominio.

## Endpoints principales

### Conexiones

- `POST /api/v1/data-sources`
- `POST /api/v1/data-sources/{id}/test`
- `GET /api/v1/data-sources`
- `PATCH /api/v1/data-sources/{id}`
- `POST /api/v1/data-sources/{id}/disable`

### Reportes

- `POST /api/v1/reports`
- `GET /api/v1/reports`
- `GET /api/v1/reports/{id}`
- `PATCH /api/v1/reports/{id}`
- `POST /api/v1/reports/{id}/versions`
- `POST /api/v1/reports/{id}/publish`
- `POST /api/v1/reports/{id}/archive`

### Creador

- `POST /api/v1/report-builder/validate`
- `POST /api/v1/report-builder/discover-columns`
- `POST /api/v1/report-builder/preview`

### Ejecucion

- `GET /api/v1/catalog/reports`
- `POST /api/v1/report-executions`
- `GET /api/v1/report-executions/{id}`
- `GET /api/v1/report-executions/{id}/rows`
- `POST /api/v1/report-executions/{id}/exports`
- `GET /api/v1/report-exports/{id}`

### Auditoria y operacion

- `GET /api/v1/audit/events`
- `GET /api/v1/operations/executions`
- `GET /api/v1/operations/metrics`

## Payloads de ejemplo

### Crear reporte

```json
{
  "name": "Ventas por cliente",
  "description": "Reporte operativo de ventas",
  "categoryId": "sales",
  "dataSourceId": "ds_pg_001"
}
```

### Validar SQL

```json
{
  "dataSourceId": "ds_pg_001",
  "sql": "SELECT c.customer_id, c.customer_name FROM customers c WHERE (:customer_id IS NULL OR c.customer_id = :customer_id)"
}
```

### Respuesta de columnas

```json
{
  "valid": true,
  "parameters": [
    {
      "name": "customer_id",
      "inferredType": "string"
    }
  ],
  "columns": [
    {
      "sourceName": "customer_id",
      "label": "customer_id",
      "dataType": "varchar"
    },
    {
      "sourceName": "customer_name",
      "label": "customer_name",
      "dataType": "varchar"
    }
  ],
  "warnings": []
}
```

### Ejecutar reporte

```json
{
  "reportId": "rpt_ventas_cliente",
  "parameters": {
    "customer_id": "C-1002",
    "date_from": "2026-01-01",
    "date_to": "2026-02-01"
  },
  "page": 1,
  "pageSize": 100
}
```

## Flujo 1: Creacion de reporte

```mermaid
sequenceDiagram
    actor Admin
    participant UI as UI Creador
    participant API as API
    participant RD as Report Definition Service
    participant QE as Query Engine
    participant DB as Source DB

    Admin->>UI: Ingresa SQL
    UI->>API: validate/discover/preview
    API->>RD: solicitar validacion
    RD->>QE: validar SQL y extraer parametros
    QE->>DB: discoverColumns + preview limitado
    DB-->>QE: metadata + filas
    QE-->>RD: columnas + preview + warnings
    RD-->>API: respuesta consolidada
    API-->>UI: mostrar preview y campos
    Admin->>UI: configura labels y filtros
    UI->>API: guardar version
```

## Flujo 2: Ejecucion por usuario final

```mermaid
sequenceDiagram
    actor User
    participant UI as UI Catalogo
    participant API as API
    participant EO as Execution Orchestrator
    participant QE as Query Engine
    participant DB as Source DB

    User->>UI: abre reporte
    UI->>API: obtiene metadata y filtros
    User->>UI: completa filtros
    UI->>API: ejecuta reporte
    API->>EO: validar permisos y limites
    EO->>QE: ejecutar SQL parametrizado
    QE->>DB: consulta paginada
    DB-->>QE: filas
    QE-->>EO: resultado
    EO-->>API: estado + filas + auditoria
    API-->>UI: renderiza tabla
```

## Flujo 3: Exportacion asincrona

```mermaid
sequenceDiagram
    actor User
    participant API as API
    participant Q as Queue
    participant W as Worker
    participant QE as Query Engine
    participant DB as Source DB

    User->>API: solicita exportacion XLSX
    API->>Q: encola job
    API-->>User: export pending
    Q->>W: entrega job
    W->>QE: ejecutar consulta completa
    QE->>DB: query
    DB-->>QE: filas
    QE-->>W: dataset
    W->>W: genera archivo
    W-->>API: actualiza estado completed
```

## Reglas de negocio API

- No se puede publicar un reporte sin preview exitoso.
- No se puede ejecutar un reporte en estado `draft`.
- Los parametros requeridos deben validarse antes de llegar al motor.
- Los errores tecnicos se traducen a codigos funcionales.
- Las exportaciones deben expirar y limpiarse automaticamente.

## Recomendacion Final

Los contratos API deben mantenerse simples y orientados al dominio. La logica sensible de validacion SQL y conexion multi-DB no debe filtrarse al frontend; el frontend solo consume metadata y resultados.

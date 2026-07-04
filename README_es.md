# Hile Reports

Base inicial del proyecto para el reporteador universal multi-DB.

## Build

- `Gradle` multi-modulo
- `settings.gradle` define los modulos del workspace
- `build.gradle` centraliza convenciones y versiones comunes

## Modulos

- `reporting-domain`: entidades y reglas puras.
- `reporting-application`: casos de uso y puertos.
- `reporting-infrastructure`: persistencia y adaptadores tecnicos.
- `reporting-connectors`: conectores Oracle, MySQL y PostgreSQL.
- `reporting-security`: autenticacion local y evolucion futura a `AD`.
- `reporting-jobs`: jobs de exportacion y mantenimiento.
- `reporting-bootstrap`: aplicacion Spring Boot y API REST.

## Documentacion

La documentacion de arquitectura y planificacion vive en `docs/architecture/reporteador-universal`.

Para continuidad de IA o de desarrollo, comienza por `AGENT_HANDOFF.md` y luego `docs/architecture/10-implementation-memory_es.md`.

## Objetivo del skeleton

Esta base no implementa el producto completo; establece los limites entre modulos y los contratos principales para acelerar el arranque del equipo.

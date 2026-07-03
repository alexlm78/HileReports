# Plan de Implementacion

## Fase 0: Definicion

- Confirmar `Spring Boot` como stack base.
- Confirmar despliegue inicial `on-premise` y lineamientos `cloud-ready`.
- Confirmar autenticacion local para MVP y estrategia posterior de integracion `AD`.
- Validar politicas de seguridad y compliance.

## Fase 1: Plataforma base

- Crear estructura modular del backend.
- Implementar autenticacion, autorizacion y auditoria base.
- Implementar metadata store.
- Crear modulo de conexiones y secretos.

## Fase 2: Motor SQL

- Implementar `QueryValidator`.
- Implementar `Connector Factory`.
- Implementar conectores para PostgreSQL y MySQL.
- Implementar conector Oracle.
- Implementar discovery de columnas y preview.

## Fase 3: Creador de reportes

- UI para crear reporte y seleccionar datasource.
- Editor SQL con validacion y preview.
- Configuracion de columnas y filtros.
- Versionado y publicacion.

## Fase 4: Catalogo y ejecucion

- Listado de reportes publicados.
- Formulario dinamico de filtros.
- Ejecucion paginada.
- Historial de ejecuciones.

## Fase 5: Exportaciones y operaciones

- Cola de trabajos y workers.
- Exportaciones CSV/XLSX.
- Monitoreo, metricas y alertas.
- Limpieza automatica de archivos temporales.

## Entregables sugeridos por sprint

| Sprint | Entregable |
|---|---|
| 1 | Estructura Spring Boot, auth local, metadata store |
| 2 | Data sources, cifrado de secretos y test de conexion |
| 3 | Validator y conectores PostgreSQL/MySQL |
| 4 | Conector Oracle, preview y discovery |
| 5 | Creador de reportes, versionado y publicacion |
| 6 | Catalogo, filtros, ejecucion y auditoria |
| 7 | Exportaciones, observabilidad y tuning para 500 concurrentes |
| 8 | Hardening, UAT y preparacion de despliegue on-premise |

## Equipo sugerido

- 1 Tech Lead / Architect.
- 2 o 3 Backend developers Java.
- 1 Frontend developer.
- 1 QA automation o SDET compartido.
- 1 DevOps parcial si el entorno aun no existe.

## Definition of Done tecnica

- Feature con pruebas automatizadas relevantes.
- Logs y metricas agregadas.
- Seguridad revisada en manejo de secretos y SQL.
- Documentacion tecnica y runbooks actualizados.

## Recomendacion Final

Implementar por fases verticales y no por capas aisladas. El primer camino de valor debe cubrir de extremo a extremo: registrar conexion, crear reporte simple, hacer preview y ejecutarlo en catalogo, ya sobre la base definitiva de `Java + Spring Boot`.

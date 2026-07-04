# Reporteador Universal

Este paquete documental define la arquitectura objetivo para un sistema de reporteo universal capaz de ejecutar reportes basados en SQL sobre multiples motores de base de datos, comenzando con Oracle, MySQL y PostgreSQL.

## Objetivo

Permitir que usuarios administradores creen reportes a partir de una consulta SQL, obtengan un preview de columnas y datos, definan filtros parametrizables y publiquen reportes listos para ejecucion por usuarios finales.

## Contenido

- `00-vision-y-requerimientos.md`: contexto, objetivos, actores y requerimientos.
- `01-opciones-arquitectonicas.md`: alternativas evaluadas y recomendacion.
- `02-arquitectura-recomendada.md`: arquitectura objetivo, componentes y diagramas.
- `03-modelo-datos-y-multidb.md`: modelo de dominio, metadata y estrategia multi-DB.
- `04-api-y-flujos.md`: contratos API y flujos funcionales.
- `05-seguridad-operacion-y-nfr.md`: seguridad, rendimiento, observabilidad y operacion.
- `06-plan-implementacion.md`: roadmap sugerido por fases.
- `07-plan-minucioso-implementacion.md`: backlog detallado por epicas, tareas, dependencias y entregables.
- `10-implementation-memory.md`: fotografia verificada de implementacion, avance y pendientes.
- `adr/ADR-001-monolito-modular.md`: decision de arquitectura base.
- `adr/ADR-002-ejecucion-sql-segura.md`: decision sobre validacion y ejecucion de SQL.
- `adr/ADR-003-conectividad-multidb.md`: decision sobre conectores y abstraccion de motores.
- `adr/ADR-004-stack-java-spring-boot.md`: decision de stack base de implementacion.
- `adr/ADR-005-autenticacion-local-y-evolucion-ad.md`: decision de autenticacion por etapas.

## Supuestos iniciales

- El sistema sera una aplicacion web empresarial interna.
- El foco de negocio inicial es reporting operativo interno para informacion especifica como ventas, traslados y procesos similares.
- El stack objetivo sera `Java` con preferencia por `Spring Boot`.
- La plataforma se desplegara inicialmente `on-premise`, preparada para una futura migracion a nube.
- La autenticacion de la primera version sera con usuarios locales, dejando una ruta de evolucion hacia `Active Directory`.
- La plataforma debe soportar hasta `500` usuarios concurrentes como objetivo de capacidad inicial.
- La primera version soportara solo consultas `SELECT` y variantes `WITH`.
- Los reportes podran ejecutarse bajo permisos controlados por el sistema y por credenciales tecnicas registradas.
- El preview tendra limite de filas configurable para evitar cargas costosas.
- La primera release no incluye diseñador pixel-perfect; el foco es tabular con exportacion.
- Los filtros del usuario final se basan en parametros declarados en el reporte y no en edicion libre del SQL.

## Decisiones confirmadas

1. El sistema sera una plataforma interna para informacion operativa especifica de las plataformas de la empresa.
2. El stack preferido es `Java`, priorizando `Spring Boot` sobre `Quarkus` para la primera version.
3. La capacidad objetivo es de hasta `500` usuarios concurrentes.
4. El despliegue inicial sera `on-premise`, con presupuesto bajo y una arquitectura preparada para evolucionar a la nube.
5. La autenticacion inicial sera local; la integracion con `AD` queda planificada para una fase posterior.

## Recomendacion ejecutiva

Se recomienda implementar una arquitectura de **monolito modular** en `Java + Spring Boot`, con capas bien separadas, motor de ejecucion asincrono y abstraccion por conectores de base de datos. Esta opcion minimiza costo inicial, facilita gobierno y seguridad del SQL, soporta el objetivo de `500` concurrentes y mantiene una ruta limpia hacia una futura separacion en servicios o migracion a nube si el volumen o la organizacion lo requieren.

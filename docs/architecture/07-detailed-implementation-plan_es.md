# Plan Minucioso de Implementacion

## Objetivo del plan

Definir una secuencia detallada de trabajo para implementar el reporteador universal con `Java + Spring Boot`, despliegue inicial `on-premise`, autenticacion local, soporte multi-DB y capacidad objetivo de hasta `500` usuarios concurrentes.

## Supuestos de ejecucion

- Equipo base: `1` Tech Lead, `2-3` backend Java, `1` frontend, `1` QA compartido, `1` DevOps parcial.
- Sprint sugerido: `2 semanas`.
- Estrategia de entrega: vertical slices funcionales.
- Base operacional del sistema: `PostgreSQL`.

## Orden maestro de implementacion

1. Fundacion tecnica.
2. Seguridad base y usuarios locales.
3. Metadata store y catalogos.
4. Data sources y conectividad segura.
5. Motor de validacion SQL.
6. Discovery y preview.
7. Creador de reportes.
8. Catalogo y ejecucion.
9. Exportaciones y jobs.
10. Observabilidad, performance y hardening.

## Epic 1: Fundacion tecnica

### Objetivo

Dejar la base del proyecto, arquitectura interna, convenciones y pipeline.

### Tareas backend

- Crear repositorio y estructura modular:
  - `app-api`
  - `app-domain`
  - `app-infrastructure`
  - `app-connectors`
  - `app-jobs`
- Configurar `Spring Boot 3.x`, `Java 21` o la version LTS aprobada.
- Configurar manejo de perfiles `local`, `dev`, `qa`, `prod`.
- Integrar `Flyway` para migraciones.
- Configurar `SpringDoc/OpenAPI`.
- Configurar manejo unificado de errores.
- Configurar logging estructurado.
- Definir convenciones de paquetes y puertos/adaptadores.

### Tareas frontend

- Crear base del frontend.
- Definir layout base: login, catalogo, builder, administracion.
- Configurar cliente HTTP, manejo de errores y guardas de ruta.

### Tareas DevOps

- Crear pipeline CI:
  - build
  - test
  - analisis estatico
  - empaquetado
- Definir artefacto de despliegue `jar` o contenedor.
- Preparar templates de variables por ambiente.

### Criterios de salida

- Proyecto compila.
- Pipeline ejecuta build y tests.
- Se publica documentacion OpenAPI base.

## Epic 2: Seguridad base y usuarios locales

### Objetivo

Habilitar autenticacion inicial con usuarios locales y dejar preparada la evolucion a `AD`.

### Tareas backend

- Implementar entidades `user`, `role`, `permission`, `user_role`.
- Implementar login con `Spring Security`.
- Implementar password hashing con `bcrypt` o algoritmo equivalente fuerte.
- Implementar refresh tokens o sesiones controladas.
- Definir permisos:
  - `USER_MANAGE`
  - `DATASOURCE_MANAGE`
  - `REPORT_DESIGN`
  - `REPORT_PUBLISH`
  - `REPORT_EXECUTE`
  - `AUDIT_VIEW`
- Crear interfaz `AuthenticationProviderPort`.
- Implementar proveedor `LocalAuthenticationProvider`.
- Diseñar stub o contrato para `AdAuthenticationProvider`.
- Implementar auditoria de login, logout y cambios de usuario.

### Tareas frontend

- Pantalla de login.
- Gestion de usuarios locales.
- Pantalla de roles y asignacion.

### Criterios de salida

- Usuarios locales pueden autenticarse.
- RBAC funcional por endpoint y por modulo.
- El dominio no depende directamente de `AD`.

## Epic 3: Metadata store y catalogos base

### Objetivo

Persistir definiciones de reportes, categorias y auditoria.

### Tareas backend

- Crear migraciones iniciales para:
  - `category`
  - `data_source`
  - `report_definition`
  - `report_version`
  - `report_column`
  - `report_parameter`
  - `report_execution`
  - `report_execution_parameter`
  - `report_export`
  - `audit_event`
- Implementar repositorios y servicios de dominio.
- Crear endpoints CRUD para categorias.
- Implementar auditoria generica de entidades.

### Criterios de salida

- Metadata persistida con versionado basico.
- Auditoria de cambios disponible.

## Epic 4: Data sources y conectividad segura

### Objetivo

Registrar y probar conexiones a Oracle, MySQL y PostgreSQL.

### Tareas backend

- Implementar cifrado de secretos en aplicacion o integracion con secret store disponible.
- Crear `DataSourceService`.
- Crear `ConnectorFactory`.
- Implementar `testConnection`.
- Definir politicas de pooling por motor.
- Registrar metadata tecnica del datasource.
- Implementar habilitar/deshabilitar datasource.

### Tareas de conectores

- Implementar `PostgreSqlConnector`.
- Implementar `MySqlConnector`.
- Implementar `OracleConnector`.
- Unificar mapeo de tipos y errores.

### Criterios de salida

- Se pueden registrar y probar los tres motores.
- Los secretos no se almacenan en texto plano.

## Epic 5: Motor de validacion SQL

### Objetivo

Garantizar que el SQL ingresado sea seguro y utilizable por el sistema.

### Tareas backend

- Implementar `QueryValidator`.
- Bloquear DDL/DML y sentencias multiples.
- Implementar extraccion de parametros nombrados.
- Validar comentarios sospechosos y patrones prohibidos.
- Agregar timeout y limites por consulta.
- Definir codigos de error funcionales.

### Tareas QA

- Crear suite de casos validos e invalidos.
- Crear casos por motor cuando existan diferencias dialectales.

### Criterios de salida

- El motor rechaza SQL inseguro.
- Los parametros se descubren correctamente.

## Epic 6: Discovery de columnas y preview

### Objetivo

Descubrir columnas y mostrar preview controlado desde el builder.

### Tareas backend

- Implementar `discoverColumns`.
- Implementar `executePreview`.
- Definir limite de preview configurable.
- Estandarizar tipos de columnas a tipos de UI.
- Persistir metadata de columnas detectadas.

### Tareas frontend

- Mostrar grid de preview.
- Mostrar lista de columnas descubiertas.
- Mostrar warnings y errores amigables.

### Criterios de salida

- El administrador puede validar visualmente columnas y datos de muestra.

## Epic 7: Creador de reportes

### Objetivo

Permitir crear, editar, versionar y publicar reportes.

### Tareas backend

- Crear `ReportDefinitionService`.
- Implementar guardar borrador.
- Implementar crear nueva version.
- Implementar publicar/despublicar.
- Validar que solo se publique si el preview fue exitoso.
- Permitir metadatos de columnas:
  - label
  - orden
  - visible
  - formato
- Permitir configuracion de parametros:
  - tipo
  - requerido
  - valor por defecto
  - multi-valor

### Tareas frontend

- Editor de metadata general del reporte.
- Editor SQL.
- Panel de preview.
- Configuracion de columnas.
- Configuracion de filtros.
- Flujo de publicacion.

### Criterios de salida

- Existe builder funcional de punta a punta.

## Epic 8: Catalogo y ejecucion de reportes

### Objetivo

Exponer reportes publicados para usuarios finales.

### Tareas backend

- Implementar listado filtrable de reportes.
- Implementar resolucion de filtros configurados.
- Implementar ejecucion paginada.
- Persistir `report_execution`.
- Aplicar ACL por reporte y datasource.
- Sanitizar errores de base de datos.

### Tareas frontend

- Pantalla de catalogo.
- Busqueda por nombre y categoria.
- Formulario dinamico de filtros.
- Grilla de resultados paginada.
- Historial de ejecuciones.

### Criterios de salida

- Un usuario puede buscar, abrir y ejecutar reportes publicados.

## Epic 9: Exportaciones y jobs

### Objetivo

Permitir exportaciones sin bloquear la experiencia interactiva.

### Tareas backend

- Implementar `ExportService`.
- Implementar scheduler o cola persistente.
- Generar CSV.
- Generar XLSX.
- Persistir estado de exportacion.
- Expirar y limpiar archivos temporales.

### Tareas frontend

- Solicitar exportacion.
- Visualizar estado del job.
- Descargar archivo terminado.

### Criterios de salida

- Exportaciones medianas y grandes se procesan asincronamente.

## Epic 10: Observabilidad, performance y hardening

### Objetivo

Preparar la plataforma para operacion real `on-premise` y carga objetivo.

### Tareas backend

- Instrumentar metricas con `Micrometer`.
- Publicar endpoint para `Prometheus`.
- Agregar health checks.
- Ajustar pools de conexiones.
- Ajustar timeouts y limites.
- Optimizar queries de metadata.
- Implementar rate limiting o quotas si aplica.

### Tareas QA/Performance

- Pruebas de carga orientadas a `500` concurrentes.
- Medicion de `p95` para endpoints criticos.
- UAT funcional con reportes reales.

### Tareas DevOps

- Runbook de despliegue `on-premise`.
- Runbook de backup y recuperacion.
- Plantillas de configuracion para futura nube.

### Criterios de salida

- La plataforma pasa pruebas de carga y operacion basica.

## Dependencias clave

| Bloque | Depende de |
|---|---|
| Seguridad local | Fundacion tecnica |
| Metadata store | Fundacion tecnica |
| Data sources | Seguridad local, metadata store |
| Validador SQL | Data sources basicos |
| Preview | Validador SQL, conectores |
| Builder | Preview, metadata store |
| Catalogo y ejecucion | Builder, seguridad |
| Exportaciones | Ejecucion |
| Hardening | Todas las anteriores |

## Backlog priorizado del MVP

### MVP-1 Fundacional

- Login local.
- Roles basicos.
- CRUD de datasource.
- Test de conexion.
- Soporte PostgreSQL y MySQL.

### MVP-2 Core de reportes

- Validacion SQL.
- Discovery de columnas.
- Preview.
- Builder.
- Publicacion de reportes.

### MVP-3 Consumo

- Catalogo.
- Filtros dinamicos.
- Ejecucion paginada.
- Auditoria.

### MVP-4 Operacion

- Oracle connector.
- Exportaciones.
- Observabilidad.
- Performance tuning.

## Riesgos de planificacion

| Riesgo | Impacto | Accion preventiva |
|---|---|---|
| Oracle retrasa por drivers o ambiente | Alto | Atacar compatibilidad en sprint temprano |
| SQL validator subestima casos complejos | Alto | Diseñar bateria de casos desde el inicio |
| Frontend builder crece demasiado | Medio | Entregar builder incremental y no WYSIWYG |
| Carga de 500 concurrentes afecta DB fuente | Alto | Limites, paginacion, export async, quotas |

## Definition of Ready para historias

- Criterio funcional claro.
- Contrato API definido o borrador validado.
- Regla de seguridad identificada.
- Datos de prueba disponibles.
- Criterio de aceptacion medible.

## Definition of Done para historias

- Codigo implementado y revisado.
- Tests relevantes aprobados.
- Logs y metricas agregados si aplica.
- Documentacion actualizada.
- Sin vulnerabilidades o errores criticos abiertos.

## Recomendacion Final

La implementacion debe comenzar por el camino vertical mas corto que valide el producto: `login local -> datasource PostgreSQL/MySQL -> SQL seguro -> preview -> builder -> catalogo -> ejecucion`. `Oracle`, exportaciones avanzadas y tuning para `500` concurrentes deben trabajarse muy temprano, pero sin bloquear la validacion inicial del flujo principal.

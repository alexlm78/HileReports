# Backlog de Implementacion

## Objetivo

Traducir la arquitectura y el plan minucioso a un backlog operativo listo para gestionarse en `Jira`, `Azure DevOps` o una herramienta equivalente.

## Convenciones sugeridas

- `EP`: epica
- `FEAT`: feature
- `US`: user story
- `TASK`: tarea tecnica
- `SPIKE`: investigacion acotada
- Prioridades: `P1`, `P2`, `P3`

## Vista de releases

| Release | Objetivo |
|---|---|
| `R1 - MVP Base` | Seguridad local, data sources, SQL seguro, preview y builder basico |
| `R2 - Consumo` | Catalogo, ejecucion, auditoria y exportaciones |
| `R3 - Hardening` | Oracle robusto, performance, observabilidad y preparacion operativa |
| `R4 - Evolucion` | Integracion `AD`, mejoras UX y ampliacion funcional |

## EP-01 Fundacion Tecnica

### FEAT-01.1 Repositorio y build multi-modulo

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-01.1.1` | User Story | Como developer quiero una estructura multi-modulo para separar dominio, aplicacion e infraestructura | P1 | - |
| `TASK-01.1.1-a` | Tarea | Crear `settings.gradle`, `build.gradle` raiz y modulos base | P1 | - |
| `TASK-01.1.1-b` | Tarea | Configurar versionado de Java y BOM de Spring Boot | P1 | `TASK-01.1.1-a` |
| `TASK-01.1.1-c` | Tarea | Configurar plugins de build y test | P1 | `TASK-01.1.1-a` |

### FEAT-01.2 Entornos y configuracion

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-01.2.1` | User Story | Como operador quiero perfiles por ambiente para desplegar sin cambiar codigo | P1 | `US-01.1.1` |
| `TASK-01.2.1-a` | Tarea | Crear `application.yml` base y perfiles `local`, `dev`, `qa`, `prod` | P1 | `US-01.1.1` |
| `TASK-01.2.1-b` | Tarea | Externalizar variables sensibles | P1 | `TASK-01.2.1-a` |

### FEAT-01.3 Calidad y pipeline

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-01.3.1` | User Story | Como equipo quiero pipeline CI para validar build y tests en cada cambio | P1 | `US-01.1.1` |
| `TASK-01.3.1-a` | Tarea | Definir pipeline de build, test y analisis estatico | P1 | `US-01.1.1` |
| `TASK-01.3.1-b` | Tarea | Publicar artefacto ejecutable | P2 | `TASK-01.3.1-a` |

## EP-02 Seguridad y Usuarios Locales

### FEAT-02.1 Login local

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-02.1.1` | User Story | Como usuario quiero iniciar sesion con credenciales locales para acceder a la plataforma | P1 | `EP-01` |
| `TASK-02.1.1-a` | Tarea | Crear entidades de usuario, rol y permiso | P1 | `EP-01` |
| `TASK-02.1.1-b` | Tarea | Configurar `Spring Security` y password hashing | P1 | `TASK-02.1.1-a` |
| `TASK-02.1.1-c` | Tarea | Implementar endpoint de autenticacion | P1 | `TASK-02.1.1-b` |

### FEAT-02.2 Autorizacion RBAC

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-02.2.1` | User Story | Como administrador quiero controlar permisos por modulo para proteger funcionalidades sensibles | P1 | `US-02.1.1` |
| `TASK-02.2.1-a` | Tarea | Definir matriz de permisos inicial | P1 | `US-02.1.1` |
| `TASK-02.2.1-b` | Tarea | Aplicar autorizacion por endpoint | P1 | `TASK-02.2.1-a` |
| `TASK-02.2.1-c` | Tarea | Aplicar permisos por reporte y datasource | P2 | `TASK-02.2.1-b` |

### FEAT-02.3 Evolucion a AD

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `SPIKE-02.3.1` | Spike | Evaluar estrategia `LDAP/AD` para fase posterior | P3 | `US-02.1.1` |
| `TASK-02.3.1-a` | Tarea | Diseñar puerto de autenticacion desacoplado | P1 | `US-02.1.1` |

## EP-03 Metadata Store y Catalogos

### FEAT-03.1 Modelo operacional

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-03.1.1` | User Story | Como sistema quiero persistir reportes, versiones y auditoria para soportar gobierno | P1 | `EP-01` |
| `TASK-03.1.1-a` | Tarea | Crear migraciones iniciales con `Flyway` | P1 | `EP-01` |
| `TASK-03.1.1-b` | Tarea | Implementar repositorios y servicios base | P1 | `TASK-03.1.1-a` |
| `TASK-03.1.1-c` | Tarea | Registrar auditoria de entidades | P2 | `TASK-03.1.1-b` |

### FEAT-03.2 Categorias y clasificacion

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-03.2.1` | User Story | Como administrador quiero organizar reportes por categorias y etiquetas | P2 | `US-03.1.1` |
| `TASK-03.2.1-a` | Tarea | CRUD de categorias | P2 | `US-03.1.1` |
| `TASK-03.2.1-b` | Tarea | Modelo de etiquetas y ownership | P3 | `US-03.1.1` |

## EP-04 Data Sources y Conectividad

### FEAT-04.1 Gestion de data sources

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-04.1.1` | User Story | Como administrador quiero registrar conexiones Oracle, MySQL y PostgreSQL para usarlas en reportes | P1 | `EP-02`, `EP-03` |
| `TASK-04.1.1-a` | Tarea | Implementar CRUD de `data_source` | P1 | `EP-03` |
| `TASK-04.1.1-b` | Tarea | Implementar cifrado de secretos | P1 | `TASK-04.1.1-a` |
| `TASK-04.1.1-c` | Tarea | Implementar `testConnection` | P1 | `TASK-04.1.1-a` |

### FEAT-04.2 Conectores multi-DB

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-04.2.1` | User Story | Como sistema quiero abstraer los motores para ejecutar reportes sin acoplar el dominio al dialecto | P1 | `US-04.1.1` |
| `TASK-04.2.1-a` | Tarea | Implementar `PostgreSqlConnector` | P1 | `US-04.1.1` |
| `TASK-04.2.1-b` | Tarea | Implementar `MySqlConnector` | P1 | `US-04.1.1` |
| `TASK-04.2.1-c` | Tarea | Implementar `OracleConnector` | P2 | `US-04.1.1` |
| `TASK-04.2.1-d` | Tarea | Implementar `ConnectorFactory` | P1 | `TASK-04.2.1-a`, `TASK-04.2.1-b` |

## EP-05 SQL Seguro

### FEAT-05.1 Validacion y parser

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-05.1.1` | User Story | Como administrador quiero validar que mi SQL sea solo lectura antes de guardar un reporte | P1 | `EP-04` |
| `TASK-05.1.1-a` | Tarea | Implementar `QueryValidator` | P1 | `EP-04` |
| `TASK-05.1.1-b` | Tarea | Bloquear DDL, DML y sentencias multiples | P1 | `TASK-05.1.1-a` |
| `TASK-05.1.1-c` | Tarea | Detectar patrones y comentarios peligrosos | P1 | `TASK-05.1.1-a` |
| `TASK-05.1.1-d` | Tarea | Extraer parametros nombrados | P1 | `TASK-05.1.1-a` |

### FEAT-05.2 Testing del validador

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-05.2.1` | User Story | Como equipo quiero pruebas automatizadas del validador para prevenir regresiones | P1 | `US-05.1.1` |
| `TASK-05.2.1-a` | Tarea | Crear matriz de casos validos e invalidos | P1 | `US-05.1.1` |
| `TASK-05.2.1-b` | Tarea | Agregar pruebas por dialecto si aplica | P2 | `TASK-05.2.1-a` |

## EP-06 Preview y Discovery

### FEAT-06.1 Descubrimiento de columnas

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-06.1.1` | User Story | Como administrador quiero descubrir columnas automaticamente a partir del SQL para configurar el reporte | P1 | `EP-05` |
| `TASK-06.1.1-a` | Tarea | Implementar `discoverColumns` | P1 | `EP-05` |
| `TASK-06.1.1-b` | Tarea | Estandarizar tipos de salida | P1 | `TASK-06.1.1-a` |

### FEAT-06.2 Preview controlado

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-06.2.1` | User Story | Como administrador quiero ver un preview limitado del resultado antes de publicar el reporte | P1 | `US-06.1.1` |
| `TASK-06.2.1-a` | Tarea | Implementar `executePreview` con limites | P1 | `US-06.1.1` |
| `TASK-06.2.1-b` | Tarea | Exponer endpoint de preview | P1 | `TASK-06.2.1-a` |

## EP-07 Builder de Reportes

### FEAT-07.1 Definicion y versionado

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-07.1.1` | User Story | Como administrador quiero crear y versionar reportes para mantener control de cambios | P1 | `EP-06` |
| `TASK-07.1.1-a` | Tarea | Implementar `ReportDefinitionService` | P1 | `EP-06` |
| `TASK-07.1.1-b` | Tarea | Guardar borrador y nueva version | P1 | `TASK-07.1.1-a` |
| `TASK-07.1.1-c` | Tarea | Publicar y despublicar reportes | P1 | `TASK-07.1.1-b` |

### FEAT-07.2 Configuracion de columnas y filtros

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-07.2.1` | User Story | Como administrador quiero definir etiquetas, visibilidad y filtros para controlar la experiencia final | P1 | `US-07.1.1` |
| `TASK-07.2.1-a` | Tarea | Configurar columnas visibles, orden y formato | P1 | `US-07.1.1` |
| `TASK-07.2.1-b` | Tarea | Configurar parametros, default y obligatoriedad | P1 | `US-07.1.1` |
| `TASK-07.2.1-c` | Tarea | Validar que solo se publique con preview exitoso | P1 | `US-07.1.1` |

## EP-08 Catalogo y Ejecucion

### FEAT-08.1 Catalogo para usuario final

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-08.1.1` | User Story | Como usuario final quiero ver un catalogo de reportes publicados para ejecutar solo los autorizados | P1 | `EP-07`, `EP-02` |
| `TASK-08.1.1-a` | Tarea | Implementar listado filtrable | P1 | `EP-07` |
| `TASK-08.1.1-b` | Tarea | Aplicar permisos por reporte | P1 | `US-08.1.1` |

### FEAT-08.2 Ejecucion paginada

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-08.2.1` | User Story | Como usuario final quiero ejecutar reportes con filtros para obtener informacion relevante | P1 | `US-08.1.1` |
| `TASK-08.2.1-a` | Tarea | Resolver filtros dinamicos | P1 | `US-08.1.1` |
| `TASK-08.2.1-b` | Tarea | Ejecutar query paginada | P1 | `TASK-08.2.1-a` |
| `TASK-08.2.1-c` | Tarea | Persistir historial de ejecuciones | P1 | `TASK-08.2.1-b` |

## EP-09 Exportaciones

### FEAT-09.1 Jobs asincronos

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-09.1.1` | User Story | Como usuario final quiero exportar resultados sin bloquear la interfaz | P2 | `EP-08` |
| `TASK-09.1.1-a` | Tarea | Implementar cola persistente o scheduler de jobs | P2 | `EP-08` |
| `TASK-09.1.1-b` | Tarea | Persistir estado del job | P2 | `TASK-09.1.1-a` |

### FEAT-09.2 Formatos de exportacion

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-09.2.1` | User Story | Como usuario final quiero exportar en CSV y XLSX para consumir informacion fuera de la plataforma | P2 | `US-09.1.1` |
| `TASK-09.2.1-a` | Tarea | Generar CSV | P2 | `US-09.1.1` |
| `TASK-09.2.1-b` | Tarea | Generar XLSX | P2 | `US-09.1.1` |
| `TASK-09.2.1-c` | Tarea | Expirar y limpiar archivos | P2 | `TASK-09.2.1-a`, `TASK-09.2.1-b` |

## EP-10 Observabilidad y Hardening

### FEAT-10.1 Observabilidad

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-10.1.1` | User Story | Como operador quiero metricas y health checks para monitorear la plataforma | P2 | `EP-08` |
| `TASK-10.1.1-a` | Tarea | Integrar `Micrometer` y `Actuator` | P2 | `EP-08` |
| `TASK-10.1.1-b` | Tarea | Publicar metricas para `Prometheus` | P2 | `TASK-10.1.1-a` |
| `TASK-10.1.1-c` | Tarea | Agregar correlation id y logging estructurado | P2 | `EP-01` |

### FEAT-10.2 Performance y capacidad

| ID | Tipo | Descripcion | Prioridad | Dependencias |
|---|---|---|---|---|
| `US-10.2.1` | User Story | Como negocio quiero soportar hasta 500 usuarios concurrentes con degradacion controlada | P2 | `EP-08`, `EP-09` |
| `TASK-10.2.1-a` | Tarea | Ejecutar pruebas de carga | P2 | `EP-08` |
| `TASK-10.2.1-b` | Tarea | Ajustar pools, timeouts y limites | P2 | `TASK-10.2.1-a` |
| `TASK-10.2.1-c` | Tarea | Definir cuotas o rate limiting si aplica | P3 | `TASK-10.2.1-b` |

## Priorizacion sugerida por sprint

| Sprint | Objetivo | Items principales |
|---|---|---|
| `Sprint 1` | Fundacion | `EP-01`, base de `EP-02` |
| `Sprint 2` | Seguridad y metadata | cierre de `EP-02`, base de `EP-03` |
| `Sprint 3` | Data sources | `EP-04` PostgreSQL/MySQL |
| `Sprint 4` | SQL seguro | `EP-05` |
| `Sprint 5` | Preview | `EP-06` |
| `Sprint 6` | Builder | `EP-07` |
| `Sprint 7` | Catalogo y ejecucion | `EP-08` |
| `Sprint 8` | Exportaciones y hardening inicial | `EP-09`, inicio de `EP-10` |
| `Sprint 9` | Oracle robusto y performance | cierre de `EP-04` Oracle y `EP-10` |

## Historias criticas del MVP

- `US-02.1.1`: login local.
- `US-04.1.1`: registro de data sources.
- `US-04.2.1`: conectores PostgreSQL/MySQL.
- `US-05.1.1`: validacion SQL segura.
- `US-06.2.1`: preview controlado.
- `US-07.1.1`: versionado y publicacion.
- `US-08.2.1`: ejecucion con filtros.

## Recomendacion Final

Este backlog debe usarse como fuente de planeacion del equipo. La mejor practica es crear primero las epicas y features en la herramienta de gestion, luego cargar las user stories criticas del MVP y finalmente derivar las tareas tecnicas por sprint, sin sobrecargar el tablero con tareas de fases muy lejanas.

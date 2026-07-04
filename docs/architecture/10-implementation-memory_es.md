# Memoria de Implementacion

## Proposito

Este documento le da a un agente de IA o a un nuevo desarrollador una fotografia verificada del estado actual de la implementacion sin necesidad de leer todo el repositorio. Complementa la arquitectura objetivo y el backlog con el estado real del codigo.

## Ultima Verificacion

- Fecha: `2026-07-04`
- Estado del repositorio: arbol con slice minimo de autenticacion HTTP local agregado sobre la base tecnica existente
- Estado del build: `./gradlew test` pasa
- Alcance de la verificacion: arbol fuente, modulos Gradle, bootstrap Spring Boot, pruebas y alineacion contra backlog

## Resumen Ejecutivo

El repositorio hoy es una **linea base compilable**, no un MVP funcional completo.

Ya existe:

- Estructura multi-modulo `Gradle` alineada con el monolito modular.
- Convenciones compartidas para `Java 21`, `Spring Boot 3.3.2` y formato con `Spotless`.
- Modulos minimos de dominio, aplicacion, infraestructura, conectores, seguridad, jobs y bootstrap.
- Configuracion Spring Boot por ambiente con perfiles `local`, `dev`, `qa` y `prod`.
- Variables de base operativa externalizadas mediante environment variables.
- Integracion de `Flyway` con una migracion inicial de metadata operativa en PostgreSQL.
- Un `compose.yaml` local para la base PostgreSQL operativa usada por la propia aplicacion.
- Un validador simple de SQL de solo lectura con una suite pequena de pruebas.
- Abstracciones de conectores y adapters stub para PostgreSQL, MySQL y Oracle.
- Un puerto de autenticacion desacoplado con adapter local y stub para `AD`.
- Un caso de uso de autenticacion cableado a `POST /api/v1/auth/login` con validacion minima y pruebas.

Todavia no existe:

- Repositorios reales sobre PostgreSQL ni entidades `JPA`.
- CRUD de datasource.
- Preview real contra bases de datos.
- Publicacion de reportes, catalogo, ejecucion, auditoria ni exportaciones.
- Modulo frontend.

## Implementacion Verificada por Modulo

### `reporting-domain`

Implementado:

- `ReportDefinition`
- `ReportStatus`
- `DataSourceType`
- `AuthenticatedUser`

Evaluacion actual:

- El dominio todavia es minimo y solo cubre los primeros conceptos de reporte y seguridad.
- No existen entidades ni value objects para usuarios, roles, permisos, categorias, metadata de datasource, ejecuciones, exportaciones ni auditoria.

### `reporting-application`

Implementado:

- `CreateReportDefinitionCommand`
- `ValidationResult`
- `ColumnMetadata`
- `CreateReportDefinitionUseCase`
- Puertos de salida:
  - `AuthenticationProviderPort`
  - `DbConnectorPort`
  - `QueryValidatorPort`
  - `ReportDefinitionRepository`
- `ReportDefinitionApplicationService`

Evaluacion actual:

- El unico caso de uso implementado es `createDraft`.
- `createDraft` valida el SQL a traves del puerto validador y persiste un borrador por medio del puerto repositorio.
- No hay casos de uso para login, gestion de datasources, orquestacion de preview, publish/unpublish, catalogo, ejecucion, auditoria ni exportaciones.

### `reporting-infrastructure`

Implementado:

- `SimpleReadOnlyQueryValidator`
- `InMemoryReportDefinitionRepository`
- Migracion inicial `Flyway` para metadata operativa
- Pruebas del validador

Evaluacion actual:

- El validador es intencionalmente simple. Permite `SELECT` y `WITH`, bloquea varios tokens DDL/DML y punto y coma, y extrae parametros nombrados con regex.
- El repositorio implementado sigue siendo solo en memoria.
- `Flyway` ahora provee un esquema inicial en PostgreSQL para categorias, datasources, metadata de reportes, ejecuciones, exportaciones y eventos de auditoria.
- Todavia no hay entidades `JPA`, ni repositorios reales sobre PostgreSQL, ni infraestructura de auditoria mas alla de la base migrada.

### `reporting-connectors`

Implementado:

- `ConnectorFactory`
- `PostgreSqlConnectorAdapter`
- `MySqlConnectorAdapter`
- `OracleConnectorAdapter`
- `BaseStubConnector`

Evaluacion actual:

- Todos los adapters de conectores son stubs.
- `testConnection` solo valida que `jdbcUrl` no venga vacio.
- `discoverColumns` devuelve metadata de ejemplo.
- `executePreview` devuelve una fila falsa de ejemplo.
- No existe conectividad JDBC real, pooling, manejo de dialectos, limites ni mapeo de errores.

### `reporting-security`

Implementado:

- `LocalAuthenticationProviderAdapter`
- `AdAuthenticationProviderStub`
- Password hashing con `BCrypt`

Evaluacion actual:

- La autenticacion esta desacoplada detras de un puerto, que es una buena base para soporte futuro de `AD`.
- El auth local hoy usa un mapa hardcodeado en memoria con `BCrypt`.
- Ya existe un login HTTP minimo sobre el provider local.
- Todavia no existe repositorio de usuarios, modelo de roles o permisos, ni manejo de token/sesion.

### `reporting-jobs`

Implementado:

- `ExportCleanupJob`

Evaluacion actual:

- Es solo un placeholder.
- No hay scheduler, cola de exportaciones, ciclo de vida de archivos ni estado persistido de jobs.

### `reporting-bootstrap`

Implementado:

- `HileReportsApplication`
- `ArchitectureController`
- `AuthController`
- `application.yml` base
- Configuracion por perfil para `local`, `dev`, `qa` y `prod`
- Configuracion JDBC y `Flyway` para la base operativa PostgreSQL
- Definicion local Docker Compose para la instancia PostgreSQL operativa
- Configuracion minima de `Spring Security`
- Wiring de beans para autenticacion local

Evaluacion actual:

- Existen `POST /api/v1/auth/login` y `GET /api/v1/architecture/modules`.
- La configuracion se externaliza por variables de entorno, con defaults razonables para el perfil local.
- La aplicacion ya puede arrancar contra una base operativa PostgreSQL y ejecutar migraciones `Flyway` al inicio.
- El login queda expuesto sin sesion persistente ni token, por lo que sirve como slice tecnico inicial y no como autenticacion final de produccion.
- Todavia no existen beans para preview, datasource CRUD ni conectores reales expuestos por API.

## Alineacion con el Backlog

Leyenda:

- `Done`: implementado con codigo utilizable en el repo
- `Partial`: existe como base, stub o slice incompleto
- `Not started`: no se encontro en el codigo

| Item del backlog | Estado | Notas |
| --- | --- | --- |
| `TASK-01.1.1-a` Crear build raiz y modulos | Done | La estructura multi-modulo `Gradle` existe |
| `TASK-01.1.1-b` Version Java y BOM Spring Boot | Done | Configurados `Java 21` y BOM Boot |
| `TASK-01.1.1-c` Plugins de build y test | Done | Existen convenciones compartidas y tests |
| `TASK-01.2.1-a` Config base y perfiles | Done | Existen config base y perfiles `local`, `dev`, `qa` y `prod` |
| `TASK-01.2.1-b` Externalizar variables sensibles | Done | La configuracion del datasource operativo se externaliza con variables de entorno |
| `TASK-01.3.1-a` Pipeline CI | Not started | No se encontraron archivos de pipeline |
| `TASK-01.3.1-b` Publicar artefacto ejecutable | Partial | La app Boot compila localmente, no hay flujo CI/release |
| `TASK-02.1.1-a` Entidades user, role, permission | Not started | Solo existe el record `AuthenticatedUser` |
| `TASK-02.1.1-b` Spring Security y hashing | Partial | Se usa `BCrypt` y ya existe config web minima de `Spring Security`, pero sin sesiones, JWT ni repositorio real de usuarios |
| `TASK-02.1.1-c` Endpoint de autenticacion | Partial | Existe `POST /api/v1/auth/login`, pero solo valida credenciales locales en memoria |
| `TASK-02.2.1-a` Matriz inicial de permisos | Not started | No hay modelo de permisos |
| `TASK-02.2.1-b` Autorizacion por endpoint | Partial | Existe proteccion basica del API; solo el login y algunos endpoints de actuator quedan publicos |
| `TASK-02.2.1-c` Permisos por reporte y datasource | Not started | No existe ACL |
| `TASK-02.3.1-a` Puerto de autenticacion desacoplado | Done | Existen puerto y adapters local/AD |
| `TASK-03.1.1-a` Migraciones Flyway | Done | `Flyway` esta configurado y existe una migracion inicial de metadata operativa |
| `TASK-03.1.1-b` Repositorios y servicios base | Partial | Solo hay repo en memoria y un servicio de aplicacion |
| `TASK-03.1.1-c` Auditoria de entidades | Not started | No hay modelo ni infraestructura de auditoria |
| `TASK-03.2.1-a` CRUD de categorias | Not started | No hay modulo ni endpoint de categorias |
| `TASK-03.2.1-b` Modelo de tags y ownership | Not started | No hay implementacion |
| `TASK-04.1.1-a` CRUD de `data_source` | Not started | No hay servicio ni API de datasource |
| `TASK-04.1.1-b` Encriptacion de secretos | Not started | No hay logica de cifrado ni storage seguro |
| `TASK-04.1.1-c` `testConnection` | Partial | Existe como stub en el contrato de conectores |
| `TASK-04.2.1-a` `PostgreSqlConnector` | Partial | Solo stub |
| `TASK-04.2.1-b` `MySqlConnector` | Partial | Solo stub |
| `TASK-04.2.1-c` `OracleConnector` | Partial | Solo stub |
| `TASK-04.2.1-d` `ConnectorFactory` | Partial | Existe la factory, pero no esta cableada a adapters reales ni API |
| `TASK-05.1.1-a` `QueryValidator` | Partial | Existe una implementacion simple |
| `TASK-05.1.1-b` Bloquear DDL, DML y multiples statements | Partial | Hay bloqueo basico por tokens |
| `TASK-05.1.1-c` Detectar patrones peligrosos y comentarios | Not started | No se encontro analisis de comentarios/patrones |
| `TASK-05.1.1-d` Extraer parametros nombrados | Done | Extraccion implementada con regex |
| `TASK-05.2.1-a` Matriz de casos validos e invalidos | Partial | Solo hay unas pocas pruebas |
| `TASK-05.2.1-b` Tests por dialecto | Not started | No hay pruebas por motor |
| `TASK-06.1.1-a` `discoverColumns` | Partial | Solo implementacion stub |
| `TASK-06.1.1-b` Estandarizar tipos de salida | Partial | Existe `ColumnMetadata`, los tipos son strings de ejemplo |
| `TASK-06.2.1-a` `executePreview` con limites | Partial | Solo implementacion stub |
| `TASK-06.2.1-b` Endpoint de preview | Not started | No existe controlador |
| `TASK-07.1.1-a` `ReportDefinitionService` | Partial | Solo creacion de borrador |
| `TASK-07.1.1-b` Guardar draft y crear nueva version | Partial | Existe guardado draft, no versionado |
| `TASK-07.1.1-c` Publicar y despublicar reportes | Not started | No existe flujo de publicacion |
| `TASK-07.2.1-a` Configurar columnas | Not started | No hay configuracion persistida |
| `TASK-07.2.1-b` Configurar parametros | Not started | No hay configuracion persistida |
| `TASK-07.2.1-c` Publicar solo con preview exitoso | Not started | No existe workflow de publicacion |
| `EP-08` Catalogo y ejecucion | Not started | No hay catalogo ni flujo de ejecucion |
| `EP-09` Exportaciones | Not started | Solo job placeholder |
| `EP-10` Observabilidad y hardening | Partial | Solo existe exposicion basica de actuator |

## Slice Vertical Disponible Hoy

El repositorio ya soporta este flujo estrecho a nivel de codigo:

1. Crear un `CreateReportDefinitionCommand`.
2. Validar el SQL como read-only con `SimpleReadOnlyQueryValidator`.
3. Persistir un draft de `ReportDefinition` en `InMemoryReportDefinitionRepository`.
4. Autenticar credenciales locales por `POST /api/v1/auth/login`.

Limitacion importante:

- El flujo de draft de reportes sigue sin exponerse por REST.
- El login HTTP no crea sesion persistente ni entrega token reutilizable.

## Huecos Mayores Antes de Tener el Camino MVP

La arquitectura recomienda este camino minimo de valor:

`login local -> datasource PostgreSQL/MySQL -> SQL seguro -> preview -> builder -> catalogo -> ejecucion`

Hoy los bloqueadores principales son:

1. No hay registro real de datasources ni manejo de secretos.
2. No hay ejecucion real de conectores contra bases.
3. No hay modelo persistente de usuarios, roles y permisos.
4. No hay base de persistencia de metadata aplicada a repositorios reales.
5. No hay API de preview.
6. No hay publicacion de reportes ni catalogo.

## Siguiente Slice Recomendado

El siguiente corte mas pragmatico es:

1. Completar auth local mas alla del slice tecnico:
   - modelo `user/role/permission`
   - persistencia de usuarios
   - sesion controlada o JWT
2. Implementar CRUD de datasource con storage cifrado de secretos.
3. Reemplazar los stubs de PostgreSQL/MySQL por adapters JDBC reales.
4. Exponer endpoints de validacion, discovery de columnas y preview.
5. Despues expandir versionado y publicacion del builder.

## Comandos Usados para Verificar

```powershell
git status --short
./gradlew test
rg --files .
rg -n "@RestController|SecurityFilterChain|@Entity|Flyway" .
```

## Protocolo de Actualizacion para Agentes Futuros

Cuando cambie la implementacion, actualiza este archivo en el mismo PR y manten vigentes estas secciones:

1. `Ultima Verificacion`
2. `Implementacion Verificada por Modulo`
3. `Alineacion con el Backlog`
4. `Siguiente Slice Recomendado`

Reglas:

- Marca un item como `Done` solo cuando el camino de codigo este implementado y sea utilizable, no solo scaffolded.
- Marca un item como `Partial` cuando exista solo como stub, placeholder, adapter en memoria o codigo interno no expuesto.
- Prefiere enlazar cambios con IDs del backlog.
- Siempre vuelve a correr al menos `./gradlew test` antes de actualizar la fotografia.

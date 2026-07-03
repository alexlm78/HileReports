# Seguridad, Operacion y NFR

## Seguridad

### Identidad y acceso

- Implementar autenticacion local en la primera version.
- Diseñar un `AuthenticationProvider` intercambiable para integrar `LDAP/AD` en una fase posterior.
- Aplicar RBAC por rol y ACL por reporte/conexion.
- Separar permisos de crear, publicar, ejecutar y auditar.

### Manejo de secretos

- Guardar credenciales de bases fuente en un secret manager o con cifrado fuerte.
- Nunca persistir passwords en texto plano.
- Rotacion de secretos soportada sin republicar reportes.

### Seguridad del SQL

- Parser o validador para permitir solo lectura.
- Bloqueo de sentencias multiples.
- Uso obligatorio de parametros tipados.
- Lista de funciones o patrones bloqueados si afectan seguridad o performance.
- Timeout por consulta y maximo de filas por contexto.

### Seguridad de datos

- Enmascarar parametros sensibles en auditoria.
- No almacenar datasets completos salvo exportaciones temporales.
- Expirar archivos exportados.
- Minimizar logs con datos personales o financieros.

## Rendimiento

### Estrategias

- Preview limitado, por ejemplo 50 o 100 filas.
- Paginacion server-side obligatoria.
- Cache de metadata de columnas y definicion de reportes.
- Pool de conexiones por motor y por datasource.
- Circuit breakers o throttling para fuentes lentas.

### Presupuestos iniciales

| Escenario | Objetivo |
|---|---|
| Listado de reportes | < 1 s |
| Preview de creador | < 5 s |
| Ejecucion interactiva paginada | < 8 s objetivo p95 |
| Concurrencia objetivo | hasta 500 usuarios concurrentes |
| Exportacion grande | asincrona |

## Disponibilidad y resiliencia

- API stateless desplegable en varias instancias `on-premise`.
- Workers escalables horizontalmente.
- Reintentos solo en fallos transitorios idempotentes.
- Desactivar un datasource sin afectar todo el sistema.
- Cola durable para exportaciones.
- Configuracion externa para migracion posterior a nube sin cambios mayores de codigo.

## Observabilidad

### Logs

- Cada request y ejecucion debe tener `correlation_id`.
- Registrar datasource, reporte, duracion, resultado y tipo de error.

### Metricas

- Tiempo de validacion SQL.
- Tiempo de preview.
- Tiempo de ejecucion por motor.
- Cantidad de ejecuciones por reporte.
- Tasa de fallos por datasource.
- Exportaciones pendientes y duracion promedio.

### Trazas

- Trazar end-to-end desde UI/API hasta Query Engine y worker.

## Costos

- Empezar con monolito modular evita costos altos de plataforma.
- Los mayores costos estaran en conectividad enterprise, observabilidad y exportaciones pesadas.
- Para mantener presupuesto bajo, evitar dependencias comerciales no esenciales en la primera fase.
- Si se requiere alta concurrencia de exportaciones, aislar workers reduce costo por nodo principal.

## Mantenibilidad

- Hexagonal o puertos/adaptadores en el backend.
- Modulos aislados por bounded context.
- Pruebas automatizadas en validacion y conectores.
- Feature flags para motores o capacidades nuevas.

## Riesgos y mitigaciones

| Riesgo | Impacto | Mitigacion |
|---|---|---|
| SQL inseguro o costoso | Alto | Parser, allowlist, timeout, limites, review de publicacion |
| Diferencias dialectales | Medio-Alto | Conectores por motor y pruebas de compatibilidad |
| Exceso de carga en DB origen | Alto | Preview limitado, paginacion, ventanas horarias, rate limit |
| Credenciales comprometidas | Alto | Secret manager, rotacion, auditoria y minimizacion de acceso |
| Exportaciones enormes | Medio | Jobs asincronos, cuotas y expiracion |

## Estrategia de testing

- Unit tests para validacion SQL y extraccion de parametros.
- Contract tests por `DbConnector`.
- Integration tests contra contenedores de MySQL/PostgreSQL.
- Integration tests controlados para Oracle segun licenciamiento y ambiente.
- E2E tests para creador, preview y ejecucion de reportes.

## Recomendacion Final

La primera version debe invertir temprano en seguridad del SQL, autenticacion local robusta, observabilidad y pruebas de conectores. Son las areas con mayor riesgo tecnico y operativo para una plataforma interna `on-premise` con objetivo de hasta `500` usuarios concurrentes.

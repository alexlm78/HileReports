# ADR-002: Validacion y Ejecucion Segura de SQL

## Estado

Aprobacion propuesta

## Contexto

El sistema permitira definir reportes desde SQL ingresado por administradores. Esto introduce riesgo de seguridad, impacto de performance y potencial afectacion a bases fuente.

## Decision

Permitir exclusivamente consultas de lectura (`SELECT` y `WITH`), con validacion previa, extraccion de parametros nombrados, bloqueo de sentencias multiples y ejecucion con limites de filas/timeout.

## Consecuencias

### Positivas

- Reduce riesgo de modificacion accidental o maliciosa.
- Hace viable exponer un creador controlado.
- Facilita observabilidad y gobierno.

### Negativas

- Algunas consultas complejas no seran soportadas inicialmente.
- Requiere invertir en parser/validador y pruebas.

## Seguimiento

- Definir catalogo de patrones permitidos y bloqueados.
- Medir falsos positivos del validador durante UAT.

# ADR-003: Abstraccion de Conectividad Multi-DB con Conectores

## Estado

Aprobacion propuesta

## Contexto

El producto debe consultar Oracle, MySQL y PostgreSQL, y potencialmente otros motores en el futuro. Las diferencias de drivers, dialectos y metadata no deben contaminar la capa de dominio.

## Decision

Implementar una `Connector Factory` con conectores dedicados por motor. Cada conector cumplira un contrato comun para test de conexion, discovery de columnas, preview y ejecucion.

## Consecuencias

### Positivas

- Aisla diferencias dialectales.
- Permite agregar nuevos motores con bajo impacto.
- Facilita pruebas por contrato.

### Negativas

- Requiere mantener adaptadores y matrices de compatibilidad.
- Algunas capacidades avanzadas de un motor no se expondran de inmediato.

## Seguimiento

- Publicar guia de implementacion de nuevos conectores.
- Mantener suite de contract tests por motor.

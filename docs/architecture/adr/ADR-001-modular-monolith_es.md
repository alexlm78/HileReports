# ADR-001: Monolito Modular como Arquitectura Base

## Estado

Aprobacion propuesta

## Contexto

Se requiere entregar un reporteador universal con creador, preview, catalogo de ejecucion, conectividad multi-DB y controles de seguridad sobre SQL. El alcance inicial no justifica la complejidad operativa de microservicios.

## Decision

Adoptar un **monolito modular** con fronteras internas claras, API stateless y workers asincronos para tareas pesadas.

## Consecuencias

### Positivas

- Entrega mas rapida.
- Menor costo operativo.
- Mayor simplicidad para evolucion del dominio.

### Negativas

- Menor aislamiento de fallos.
- Requiere disciplina para evitar acoplamiento entre modulos.

## Seguimiento

- Revisar esta decision si el equipo supera varios squads o si la carga de ejecucion/exportacion exige aislamiento independiente.

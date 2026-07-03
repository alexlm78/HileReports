# ADR-004: Java con Spring Boot como Stack Base

## Estado

Aprobacion propuesta

## Contexto

El sistema debe soportar Oracle, MySQL y PostgreSQL, operar inicialmente `on-premise`, mantener bajo costo y ofrecer una ruta clara a evolucion futura. El stack favorito del equipo es `Java`, con preferencia entre `Spring` y `Quarkus`.

## Decision

Adoptar **Java + Spring Boot** como stack base de la primera version.

## Consecuencias

### Positivas

- Excelente madurez en conectividad enterprise.
- Integracion natural con `Spring Security`, observabilidad y scheduling.
- Menor riesgo de implementacion para un producto con varias preocupaciones transversales.
- Facil soporte para empaquetado portable y despliegue `on-premise`.

### Negativas

- Mayor consumo y verbosidad que otras alternativas.
- Tiempos de arranque mayores frente a `Quarkus`.

## Seguimiento

- Revisar `Quarkus` si el footprint o los tiempos de arranque se vuelven prioritarios en una fase futura.

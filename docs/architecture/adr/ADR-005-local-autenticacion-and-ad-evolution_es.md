# ADR-005: Autenticacion Local Inicial con Evolucion a Active Directory

## Estado

Aprobacion propuesta

## Contexto

La primera version debe salir con bajo costo y dependencias controladas, pero el objetivo a mediano plazo es integrarse con el usuario de red corporativo.

## Decision

Implementar autenticacion local en la primera version, encapsulada detras de un puerto de autenticacion, dejando preparada la integracion futura con `LDAP/Active Directory`.

## Consecuencias

### Positivas

- Reduce dependencias externas para el MVP.
- Permite avanzar aunque la integracion corporativa no este lista.
- Evita acoplar la seguridad de dominio a un proveedor especifico desde el inicio.

### Negativas

- Se debera migrar o federar usuarios en una fase posterior.
- El equipo debera mantener temporalmente administracion local de usuarios.

## Seguimiento

- Diseñar desde el inicio los identificadores de usuario y roles para facilitar transicion a `AD`.
- Agregar pruebas de integracion con `LDAP/AD` en la fase posterior.

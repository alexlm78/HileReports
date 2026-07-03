# Opciones Arquitectonicas

## Contexto de decision

El sistema necesita:

- soportar multiples motores de base de datos,
- ejecutar SQL de lectura con seguridad,
- ofrecer un creador con preview,
- y escalar razonablemente sin complejidad innecesaria.

## Opcion 1: Monolito modular con workers

Aplicacion unica con modulos internos bien delimitados: autenticacion, catalogo, diseno de reportes, ejecucion, conectores, auditoria y exportaciones. Los trabajos pesados se derivan a workers asincronos.

### Pros

- Menor complejidad operativa y de despliegue.
- Time-to-market mas rapido.
- Transacciones y consistencia sencillas en metadata.
- Facil de gobernar para un equipo pequeno o mediano.
- Permite evolucionar por modulos hacia servicios independientes.

### Contras

- Menor aislamiento de fallos que una arquitectura de microservicios.
- Crecimiento del codigo requiere disciplina arquitectonica.
- Escalado fino por componente es mas limitado.

### Cuando elegirla

- Equipos de 4 a 12 developers.
- Necesidad de salir rapido con riesgo controlado.
- Dominio aun en evolucion.

## Opcion 2: Microservicios por capacidad

Servicios separados para catalogo, creador de reportes, ejecucion, conectividad y exportaciones; integrados via API y eventos.

### Pros

- Escalado independiente por capacidad.
- Aislamiento de fallos mejorado.
- Equipos grandes pueden trabajar con mayor autonomia.
- Facil separar cargas intensivas de ejecucion y exportacion.

### Contras

- Complejidad operacional mucho mayor.
- Requiere DevOps, observabilidad y gobierno maduros.
- Mayor costo de desarrollo inicial.
- La consistencia de metadata y versionado es mas compleja.

### Cuando elegirla

- Alta escala desde el dia uno.
- Multiples equipos y ownership claro por dominio.
- Exigencias fuertes de aislamiento y resiliencia.

## Opcion 3: Backend monolitico con motor de consulta desacoplado

La UI y la gestion de metadata viven en una aplicacion central, mientras que la validacion y ejecucion SQL se encapsulan en un motor separado con API propia.

### Pros

- Buen equilibrio entre simplicidad y aislamiento del componente mas sensible.
- Permite evolucionar el motor hacia servicio especializado.
- Facil reforzar controles de seguridad en el motor.

### Contras

- Incrementa la complejidad frente al monolito puro.
- Duplica parte de las preocupaciones operativas.
- Puede ser prematuro si el volumen inicial es moderado.

### Cuando elegirla

- Riesgo alto percibido en el subsistema de ejecucion SQL.
- Necesidad de aislar redes o credenciales tecnicas.

## Comparativa

| Criterio | Opcion 1 | Opcion 2 | Opcion 3 |
|---|---|---|---|
| Velocidad de entrega | Alta | Media-Baja | Media |
| Complejidad operativa | Baja | Alta | Media |
| Escalado independiente | Medio | Alto | Alto en motor |
| Costo inicial | Bajo | Alto | Medio |
| Seguridad del motor SQL | Media-Alta | Alta | Alta |
| Mantenibilidad inicial | Alta | Media | Media-Alta |
| Ajuste a alcance actual | Muy alto | Bajo-Medio | Alto |

## Recomendacion Final

Se recomienda la **Opcion 1: monolito modular con workers asincronos**, con una frontera interna fuerte alrededor del motor de validacion y ejecucion SQL. Esta arquitectura es la mejor relacion entre simplicidad, seguridad, costo y mantenibilidad para una primera version empresarial con soporte multi-DB.

La recomendacion incorpora dos principios para no quedar bloqueados:

- Definir interfaces explicitas para `QueryValidator`, `QueryExecutor`, `DbConnector` y `ReportPublisher`.
- Mantener el subsistema de ejecucion desacoplado logicamente para extraerlo mas adelante si la carga, el riesgo o la estructura del equipo lo justifican.

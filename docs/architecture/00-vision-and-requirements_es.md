# Vision y Requerimientos

## Problema

Las organizaciones suelen tener informacion distribuida en multiples motores de base de datos y requieren construir reportes operativos sin depender de desarrollos puntuales para cada necesidad. En este caso el foco es una plataforma interna para mostrar informacion especifica de negocio, como ventas, traslados y otros procesos operativos dentro de las plataformas de la empresa. Se busca una plataforma unica donde:

- un administrador defina un reporte a partir de una consulta SQL,
- el sistema descubra columnas y permita previsualizar el resultado,
- se publiquen filtros parametrizados para el usuario final,
- y el usuario solo vea un catalogo de reportes disponibles para ejecutar.

## Objetivos del producto

- Unificar la creacion y ejecucion de reportes SQL sobre Oracle, MySQL y PostgreSQL.
- Separar la experiencia de creacion de la experiencia de consumo.
- Proteger las bases fuente mediante validacion, control de acceso y limites operativos.
- Resolver necesidades operativas internas con bajo costo de implementacion y operacion.
- Permitir evolucionar a nuevos motores sin rediseñar el producto.
- Habilitar auditoria, trazabilidad y gobierno de reportes.

## Actores

- `Administrador de reportes`: crea, prueba, versiona y publica reportes.
- `Usuario final`: ejecuta reportes publicados con filtros permitidos.
- `Auditor/Soporte`: revisa ejecuciones, errores, tiempos y uso.
- `Administrador de plataforma`: gestiona conexiones, seguridad y politicas.

## Requerimientos funcionales

### RF-01 Gestion de conexiones

- Registrar conexiones a Oracle, MySQL y PostgreSQL.
- Almacenar credenciales cifradas.
- Probar conectividad y metadata basica.
- Asociar permisos de uso por equipo, area o rol.

### RF-02 Creador de reportes

- Crear un reporte con nombre, descripcion, categoria y origen de datos.
- Ingresar una consulta SQL.
- Validar que la consulta sea solo de lectura.
- Obtener columnas derivadas del SQL.
- Mostrar preview con limite de filas configurable.
- Permitir mapear metadatos de columnas: etiqueta, tipo UI, formato, visible, orden.
- Definir filtros a partir de parametros seguros del reporte.
- Publicar, despublicar y versionar reportes.

### RF-03 Catalogo y ejecucion

- Listar reportes publicados visibles para el usuario.
- Buscar por nombre, categoria y etiquetas.
- Solicitar filtros configurados para el reporte.
- Ejecutar el reporte con paginacion.
- Exportar resultados a CSV y XLSX.
- Guardar historial de ejecuciones.

### RF-04 Gobierno y auditoria

- Registrar quien creo, modifico, publico y ejecuto cada reporte.
- Registrar SQL versionado y cambios de definicion.
- Mostrar errores de ejecucion sin exponer detalles sensibles.
- Permitir desactivar reportes o conexiones comprometidas.

### RF-05 Administracion operativa

- Configurar timeouts, maximo de filas preview y limites de exportacion.
- Configurar ventanas de mantenimiento y restricciones por horario.
- Visualizar metricas de uso y fallos.

## Requerimientos no funcionales

### RNF-01 Seguridad

- Solo consultas de lectura.
- Cifrado de secretos en reposo.
- TLS hacia servicios y, cuando sea posible, hacia bases fuente.
- RBAC por modulo, reporte y conexion.
- Autenticacion inicial con usuarios locales gestionados por la aplicacion.
- Diseño compatible con integracion futura a `Active Directory`.
- Auditoria completa de cambios y ejecuciones.
- Sanitizacion de errores para no exponer esquemas, secretos ni SQL sensible.

### RNF-02 Rendimiento

- Preview en menos de 5 segundos para consultas medianas sobre datasets acotados.
- Paginacion server-side para resultados interactivos.
- Exportaciones pesadas via procesos asincronos.
- Limites configurables por consulta y por usuario.

### RNF-03 Escalabilidad

- Escalado horizontal del API y de workers.
- Posibilidad de agregar nuevos conectores sin modificar el dominio principal.
- Soporte inicial para hasta 500 usuarios concurrentes y crecimiento posterior sin rediseño mayor.

### RNF-04 Mantenibilidad

- Separacion clara entre dominio, conectores, seguridad y UI.
- Contratos API estables.
- ADRs y trazabilidad de decisiones.
- Cobertura automatizada en validacion SQL, filtros y conectores.

### RNF-05 Disponibilidad y soporte

- Reintentos controlados en fallos transitorios.
- Trazas correlacionadas por ejecucion.
- Tablero de observabilidad con metricas de latencia y error rate.

### RNF-06 Despliegue y portabilidad

- Despliegue inicial `on-premise`.
- Empaquetado y configuracion listos para migracion futura a nube.
- Dependencias de infraestructura minimizadas para reducir costo inicial.
- Preferencia por componentes open source o ya estandarizados en la empresa.

## Alcance de la primera version

- Motores: Oracle, MySQL, PostgreSQL.
- Tipos de reporte: tabular.
- Filtros: texto, numerico, fecha, lista, rango, booleano.
- Exportaciones: CSV y XLSX.
- Ejecucion interactiva y ejecucion asincrona para exportaciones grandes.
- Autenticacion local con administracion interna de usuarios y roles.

## Fuera de alcance inicial

- ETL o replicacion de datos.
- Reportes visuales complejos tipo dashboard.
- SQL de escritura o procedimientos almacenados con efectos laterales.
- Multi-tenant externo con aislamiento regulatorio estricto.
- Designer WYSIWYG de layout avanzado.

## Criterios de aceptacion de arquitectura

- Agregar un nuevo motor debe requerir crear un conector sin reescribir el dominio.
- Ningun usuario final puede editar SQL libremente al ejecutar un reporte.
- Todo reporte publicado debe tener preview validado y metadatos de columnas consistentes.
- Toda ejecucion debe quedar auditada con usuario, tiempo, filtros y resultado.
- El sistema debe sostener una carga objetivo de hasta 500 usuarios concurrentes con degradacion controlada.

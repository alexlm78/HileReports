# Migration: Java 21 → Java 25 (LTS)

Checklist reproducible en otros proyectos Gradle + Spring Boot. Verificado en HileReports (`./gradlew test` verde con JDK 25.0 Zulu).

## Por qué hay que subir más que el JDK

| Componente | Antes | Después | Motivo |
|---|---|---|---|
| JDK | 21 | **25** | objetivo |
| Gradle wrapper | 8.14.5 | **9.8.1** | Gradle 8.x no corre sobre JDK 25 (mín. Gradle 9.1) |
| Spring Boot | 3.3.2 | **3.5.7** | Spring Framework 6.1 (Boot 3.3) no lee bytecode clase 69 (Java 25). Boot 3.5.x = Spring 6.2.x con soporte oficial JDK 25 |
| dependency-management plugin | 1.1.6 | 1.1.7 | alineado con Gradle 9 |
| springdoc-openapi | 2.6.0 | 2.8.13 | línea 2.8.x es la compatible con Boot 3.5 / Spring 6.2 |

Sin cambios necesarios: POI 5.3.0, jjwt 0.12.6, ojdbc11, mysql-connector-j, flyway/postgres (gestionados por BOM), Spotless 8.11.0.

## Archivos modificados

| Archivo | Cambio |
|---|---|
| `.sdkmanrc` | `java=21-zulu` → `java=25-zulu` (y `gradle=9.8.1`) |
| `gradle/wrapper/gradle-wrapper.properties` | `distributionUrl` → `gradle-9.8.1-bin.zip` |
| `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat` | regenerados con `./gradlew wrapper --gradle-version 9.8.1` |
| `build.gradle` | `JavaVersion.VERSION_21` → `VERSION_25` (source/target); `options.release = 25`; plugin Boot `3.5.7`; BOM `spring-boot-dependencies:3.5.7`; dependency-management `1.1.7` |
| `reporting-bootstrap/build.gradle` | springdoc `2.6.0` → `2.8.13` |
| `reporting-bootstrap/src/test/.../AuthControllerTest.java` | `@MockBean` → `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`); mismo uso, solo cambia el import y la anotación |
| `docs/architecture/10-implementation-memory*.md` | snapshot, fila `TASK-01.1.1-d` y sección "Java 25 Migration" |
| `Dockerfile` | `eclipse-temurin:21-jdk-alpine` / `21-jre-alpine` → `25-…` |
| `.github/workflows/ci.yml` | `actions/setup-java` `java-version: "25"` (temurin) |
| `docs/architecture/*`, `CLAUDE.md`, `AGENT_HANDOFF.md` | menciones "Java 21" → "Java 25" |

## Pasos (orden)

1. Instalar JDK: `sdk install java 25-zulu` (o `sdk env install` con `.sdkmanrc` actualizado).
2. Exportar `JAVA_HOME` al JDK 25 (`sdk env`).
3. Subir wrapper: `./gradlew wrapper --gradle-version 9.8.1` (correr 2 veces si el primer wrapper es viejo).
4. Editar `build.gradle` (versiones Java, Boot, BOM, dependency-management).
5. Subir libs con línea incompatible (springdoc). Revisar con `./gradlew dependencies` las que no vienen del BOM.
6. Dockerfile + CI.
7. Validar: `./gradlew clean test`, luego `docker build .`, luego `bootRun` con la BD.

## Notas / gotchas

- `@MockBean` / `@SpyBean` quedan **deprecated for removal** en Boot 3.4+. Ya migrado aquí a `@MockitoBean` / `@MockitoSpyBean` (`org.springframework.test.context.bean.override.mockito`); en otros proyectos buscar con `rg "MockBean|SpyBean"`. Obligatorio antes de Boot 4.
- Warning JVM `Sharing is only supported for boot loader classes…` en tests: viene del agente Mockito/ByteBuddy (CDS), inocuo. Si molesta: `-Xshare:off` en `jvmArgs` de `Test`.
- JDK 24+ avisa por acceso nativo/`sun.misc.Unsafe`; cualquier lib que falle con "restricted method" necesita `--enable-native-access=ALL-UNNAMED`. No fue necesario aquí.
- Gradle 9 aviso `Configuration.setVisible` deprecado viene del plugin de Spring Boot; desaparece con Boot más nuevo.
- Spotless `spotlessCheck` con JDK 25 funcionó con Eclipse JDT config existente; ningún cambio de formato por la migración.
- Gradle 9 + Java 25: `sourceCompatibility` y `options.release` conviven; alternativa moderna: `java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }`.
- Siguiente salto opcional: Spring Boot 4.0 (Spring 7, Jackson 3, Hibernate 7) — migración aparte.

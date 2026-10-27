# Gymit

Proyecto de grupo de **Seguridad en Entornos Móviles** (Grado en Ciberseguridad e IA · ETSII · UMA).
Aplicación Android que se construye en el Bloque II, se protege en el Bloque III y es auditada por otro grupo en el Bloque IV.

## Equipo

| | |
|---|---|
| **Grupo** | K |
| **Nombre del equipo** | GymIt |
| **Dominio asignado** | Acceso |

| Miembro | Usuario de GitHub |
|---|---|
| Alberto Bartolomé Iruela | alman368 |
| Mario Marín Hinojosa | mmarhin |
| Iván Mora Gómez | ivanmora17 |
| Alberto Neumann García | albertng31 |

## Descripción de la aplicación

_TODO (2–3 frases): qué hace exactamente la aplicación dentro del dominio asignado._

**Dato más sensible:** _TODO: el dato concreto que más daño haría si se filtrara. Es el punto de partida del modelo de amenazas del Bloque III._

## Requisitos técnicos mínimos

| Requisito | Cómo lo cumple Gymit | Responsable |
|---|---|---|
| Autenticación de usuario | _TODO_ | Iván Mora |
| Almacenamiento local de datos sensibles | _TODO_ | Alberto Bartolomé |
| Llamada a una API remota | _TODO_ | Mario Marín |
| Permiso peligroso justificado | _TODO: permiso y por qué lo necesita el dominio_ | Alberto Neumann |

## Convenciones del equipo

### Identidad y versiones del SDK

| Decisión | Valor |
|---|---|
| Nombre de paquete (`applicationId` y `namespace`) | `es.uma.seguridad.gymit` |
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 37 (Android 17) |
| `compileSdk` | 37 |

**Justificación del `targetSdk`.** Declaramos la API 37 porque es la versión estable más reciente de Android. Con ella el sistema aplica a la aplicación todas las restricciones de seguridad y privacidad vigentes, porque muchas sólo se activan si la app las declara como objetivo. Además supera el mínimo que Google Play exige hoy a las aplicaciones nuevas (API 36). Un `targetSdk` más bajo dejaría activos comportamientos heredados que en una auditoría contarían como hallazgo.

**Justificación del `minSdk`.** La API 26 cubre prácticamente todo el parque de dispositivos en uso y es la mínima que proponen las plantillas actuales de Android Studio. Permite usar iconos adaptativos y APIs criptográficas modernas sin arrastrar código de compatibilidad.

### Entorno de construcción

- Kotlin + Jetpack Compose, scripts de Gradle en Kotlin DSL y versiones centralizadas en `gradle/libs.versions.toml`.
- Gradle Wrapper 9.8.2 fijado con `distributionSha256Sum`, de modo que la distribución descargada se verifica contra su hash oficial. Construid siempre con `./gradlew` (o `.\gradlew.bat` en Windows), nunca con un Gradle del sistema.
- Para ejecutar Gradle hace falta un JDK 17 o superior. El de Android Studio sirve.

```bash
./gradlew assembleDebug      # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # app/build/outputs/apk/release/app-release-unsigned.apk
```

### Ramas y revisión

- `main` es la rama estable y siempre compila. No se hace push directo a `main`.
- Cada tarea se hace en su propia rama, que sale de `main`: `feat/<tema>`, `fix/<tema>`, `docs/<tema>`, `chore/<tema>`.
- La integración se hace por Pull Request con **al menos una revisión aprobada** de otro miembro, y la rama tiene que compilar (`./gradlew assembleDebug`) antes de fusionarse.
- Los mensajes de commit siguen [Conventional Commits](https://www.conventionalcommits.org/es/): `feat: añadir pantalla de login`, `fix: …`, `docs: …`, `chore: …`.

### Secretos y firma

- **Nunca** se suben al repositorio almacenes de claves (`*.jks`, `*.keystore`), contraseñas, `local.properties` ni claves de API, tampoco en un commit que después se revierta, porque el histórico conserva todo. El `.gitignore` ya los excluye.
- Si un secreto llega al repositorio por error, se considera comprometido: se rota y se avisa al equipo.
- La configuración de firma de la variante de publicación se leerá de un fichero local fuera de control de versiones (Bloque III).

## Reconocimiento de una aplicación real (apartado 5.4)

**Aplicación analizada:** _TODO: nombre_ · **Paquete:** `_TODO_` · **Versión:** _TODO_

| Qué mirar | Con qué | Observación |
|---|---|---|
| Permisos declarados | `aapt2 dump permissions` | _TODO_ |
| Componentes exportados | `apktool` + lectura del manifiesto | _TODO_ |
| Esquemas de firma | `apksigner verify --print-certs -v` | _TODO_ |
| `minSdk` y `targetSdk` | `aapt2 dump badging` | _TODO_ |
| Número de piezas instaladas | `adb shell pm path` | _TODO: número y sufijo de cada split_ |

**Notas para el cuaderno del equipo**

- ¿Pide algún permiso que no se pueda justificar por lo que hace la app? _TODO_
- ¿Cuántos componentes tiene exportados y cuáles parecen innecesarios? _TODO_

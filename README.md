# Gymit

Proyecto de grupo de **Seguridad en Entornos Móviles** (Grado en Ciberseguridad e IA · ETSII · UMA).
Aplicación Android que se construye en el Bloque II, se protege en el Bloque III y es auditada por otro grupo en el Bloque IV.

## Equipo

| | |
|---|---|
| **Grupo** | K |
| **Nombre del equipo** | GymIt |
| **Dominio** | Acceso (elegido por el equipo) |

| Miembro | Usuario de GitHub |
|---|---|
| Alberto Bartolomé Iruela | alman368 |
| Mario Marín Hinojosa | mmarhin |
| Iván Mora Gómez | ivanmora17 |
| Alberto Neumann García | albertng31 |

## Descripción de la aplicación

Gymit es el carné digital del gimnasio: para entrar, el socio enseña en el torno un código QR firmado que cambia cada 30 segundos y que solo se genera en el móvil vinculado a su cuenta. El servidor comprueba la firma, la caducidad y que el código no se haya usado antes, y concede o deniega el acceso. La app incluye también el alta del socio con un selfie en directo y el historial de sus entradas.

**Dato más sensible:** la credencial de acceso, es decir, la clave privada con la que el móvil firma los QR. Quien la obtenga puede generar QR válidos y entrar en el gimnasio haciéndose pasar por el socio. Por eso se genera en el Android Keystore y no sale nunca del dispositivo.

## Requisitos técnicos mínimos

| Requisito | Cómo lo cumple Gymit | Responsable |
|---|---|---|
| Autenticación de usuario | Login con correo y contraseña contra Supabase Auth, y huella (BiometricPrompt) para desbloquear el pase. La cuenta queda vinculada a un único móvil: el que registró su clave pública al darse de alta. | Iván Mora |
| Almacenamiento local de datos sensibles | La clave privada de firma vive en el Android Keystore, no exportable y ligada a la autenticación biométrica. El perfil, el selfie y el historial de accesos se guardan cifrados en el almacenamiento privado de la app. | Alberto Bartolomé |
| Llamada a una API remota | Supabase: Auth para la sesión, PostgreSQL con Row Level Security para que cada socio solo vea sus datos, y una Edge Function que valida el QR en el torno (firma, caducidad y nonce de un solo uso). | Mario Marín |
| Permiso peligroso justificado | `CAMERA`, usada con CameraX para hacer el selfie en directo durante el alta. Permite comprobar en el torno que quien entra es el titular, y no se acepta una foto subida desde la galería. | Alberto Neumann |

### Diseño de seguridad previsto

- **QR firmado:** contiene el usuario, la hora, un nonce y la firma. El servidor rechaza los QR caducados (más de 30 s) y los nonces ya usados.
- **Vinculación al dispositivo:** Key Attestation al darse de alta, para que la clave pública que se registra corresponda a una clave en hardware de ese móvil.
- **Endurecimiento:** `FLAG_SECURE` en la pantalla del pase, certificate pinning hacia Supabase, R8, detección de root y `allowBackup=false`.
- Play Integrity queda descartado porque exige publicar en Play Console.

**Pantallas:** login, alta con selfie, pase con el QR y cuenta atrás, e historial. El torno es una app aparte (o una web) que lee el QR y muestra verde o rojo.

## Convenciones del equipo

### Identidad y versiones del SDK

| Decisión | Valor |
|---|---|
| Nombre de paquete (`applicationId` y `namespace`) | `es.uma.seguridad.gymit` |
| `minSdk` | 30 (Android 11) |
| `targetSdk` | 37 (Android 17) |
| `compileSdk` | 37 |

**Justificación del `targetSdk`.** Declaramos la API 37 porque es la versión estable más reciente de Android. Con ella el sistema aplica a la aplicación todas las restricciones de seguridad y privacidad vigentes, porque muchas sólo se activan si la app las declara como objetivo. Además supera el mínimo que Google Play exige hoy a las aplicaciones nuevas (API 36). Un `targetSdk` más bajo dejaría activos comportamientos heredados que en una auditoría contarían como hallazgo.

**Justificación del `minSdk`.** La API 30 es la primera que ofrece de forma uniforme lo que necesita la credencial de acceso: claves del Keystore ligadas a la autenticación biométrica fuerte (`setUserAuthenticationParameters` con `BIOMETRIC_STRONG`), Key Attestation fiable para demostrar al servidor que la clave está en hardware, y permisos de cámara de un solo uso. Bajar de 30 obligaría a mantener rutas de compatibilidad con garantías más débiles justo en la parte crítica de la app, y Android 11 o superior cubre la gran mayoría de los móviles en uso.

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

**Aplicación analizada:** Technogym · **Paquete:** `com.technogym.tgapp` · **Versión:** 3.45.5 (versionCode 3436).
Se extrajo con `adb pull` de un Pixel 9 Pro con Android 17, el 27/10/2026.

Es la app de gimnasio más cercana a Gymit que teníamos instalada. Su flujo de QR va al revés que el nuestro: el móvil escanea el código de la máquina, mientras que en Gymit es el torno el que lee el QR del móvil.

| Qué mirar | Con qué | Observación |
|---|---|---|
| Permisos declarados | `aapt2 dump permissions` | **63 permisos solicitados** (`uses-permission`), y además define dos propios. Entre ellos hay varios peligrosos: `CAMERA`, `RECORD_AUDIO`, `ACCESS_FINE_LOCATION` y `ACCESS_BACKGROUND_LOCATION`, `READ_PHONE_STATE`, `READ_CALENDAR` / `WRITE_CALENDAR`, `READ_EXTERNAL_STORAGE`, `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` y diez de Health Connect (`health.READ_*`). También declara `AD_ID` y `ACCESS_ADSERVICES_*` para publicidad, y 15 permisos de insignias de lanzadores de otros fabricantes (Samsung, Huawei, HTC, Sony…), que vienen de una librería. |
| Componentes exportados | `aapt2 dump xmltree` sobre el manifiesto (equivale a `apktool`) | Tiene **13 componentes exportados** de 64 (2 activities, 1 alias, 4 servicios y 6 receivers). **6 no exigen ningún permiso**: `MainActivity`, con deep links `https` hacia `tgapp-link.mywellness.com`, `www.technogym.com` y `tgapp.onelink.me` más los esquemas propios `tgapp://`, `tgapplink://` y `tgappthirdparties://`; `HealthConnectPermissionsRationaleActivity`; `AudioService` y `MediaButtonReceiver`; `HealthDataSdkService`; y `no.nordicsemi…PendingIntentReceiver` (escaneo BLE). `allowBackup=false`. |
| Esquemas de firma | `apksigner verify --print-certs -v` | Solo está verificada la **firma v3**, más una SourceStamp de Google Play. El certificado es de Technogym S.p.A. con una clave **RSA de 1024 bits**, por debajo de los 2048 bits que se recomiendan hoy. |
| `minSdk` y `targetSdk` | `aapt2 dump badging` | `minSdk` 29 (Android 10), `targetSdk` 36 (Android 16) y `compileSdk` 36. |
| Número de piezas instaladas | `adb shell pm path` | **5 piezas** (App Bundle): `base.apk`, `split_config.arm64_v8a.apk` (librerías nativas), `split_config.en.apk` y `split_config.es.apk` (idiomas) y `split_config.xxhdpi.apk` (densidad de pantalla). |

**Notas para el cuaderno del equipo**

- **Permisos difíciles de justificar** por lo que hace la app: `READ_PHONE_STATE`, `RECORD_AUDIO`, la ubicación en segundo plano, el calendario y los identificadores de publicidad. `CAMERA` sí se justifica, porque sirve para escanear el QR de las máquinas (la app incluye `libbarhopper_v3.so`, de ML Kit). Bluetooth también, porque se conecta con los equipos.
- **Componentes exportados:** 13, y 6 sin permiso. La mayoría vienen de librerías (Flutter, `audio_service`, Health Connect, WorkManager, Firebase), lo que confirma lo que vimos en el paso 9: el manifiesto final incluye mucho que el equipo no escribió. El más dudoso es el `PendingIntentReceiver` de Nordic, que está exportado sin permiso. Lo segundo que más superficie abre son los deep links de `MainActivity`.
- **Para la auditoría:** está escrita en **Flutter**. La lógica va compilada en `libapp.so` (Dart AOT), así que jadx solo muestra el envoltorio Java. Además incluye `libtoolChecker.so` (RootBeer), es decir, **detección de root**, como la que tenemos prevista para Gymit.
- **Lo que no repetiremos en Gymit:** firmar con una clave de 1024 bits, pedir permisos que no usamos y exportar componentes sin permiso.

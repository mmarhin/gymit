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

- Kotlin + Jetpack Compose, scripts de Gradle en Kotlin DSL y versiones centralizadas en `gradle/libs.versions.toml`. Room se compila con KSP 2.3.12; desde la serie 2.3 la versión de KSP ya no lleva delante el número de Kotlin.
- Gradle Wrapper 9.8.2 fijado con `distributionSha256Sum`, de modo que la distribución descargada se verifica contra su hash oficial. Construid siempre con `./gradlew` (o `.\gradlew.bat` en Windows), nunca con un Gradle del sistema.
- Para ejecutar Gradle hace falta un JDK 17 o superior. El de Android Studio sirve.

### Ramas y revisión

- `main` es la rama estable y siempre compila. No se hace push directo a `main`.
- Cada tarea se hace en su propia rama, que sale de `main`: `feat/<tema>`, `fix/<tema>`, `docs/<tema>`, `chore/<tema>`.
- La integración se hace por Pull Request con **al menos una revisión aprobada** de otro miembro, y la rama tiene que compilar (`./gradlew assembleDebug`) antes de fusionarse.
- Los mensajes de commit siguen [Conventional Commits](https://www.conventionalcommits.org/es/): `feat: añadir pantalla de login`, `fix: …`, `docs: …`, `chore: …`.

### Secretos y firma

- **Nunca** se suben al repositorio almacenes de claves (`*.jks`, `*.keystore`), contraseñas, `local.properties` ni claves de API, tampoco en un commit que después se revierta, porque el histórico conserva todo. El `.gitignore` ya los excluye.
- Si un secreto llega al repositorio por error, se considera comprometido: se rota y se avisa al equipo.
- La configuración de firma de la variante de publicación se leerá de un fichero local fuera de control de versiones (Bloque III).

## Cómo compilar y ejecutar

1. **Clonar y compilar.** Al abrir el proyecto, Android Studio crea `local.properties` con la ruta del SDK. Por línea de órdenes:

   ```bash
   git clone git@github.com:mmarhin/gymit.git && cd gymit
   ./gradlew assembleDebug      # app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Arrancar el backend de la asignatura.** No está en este repositorio: es el material común del campus virtual (`Material comun backend y proyecto base.zip`, carpeta `backend/`), y los usuarios de prueba están en su `LEEME.md`.

   ```bash
   python3 servidor.py          # o: java Servidor.java
   curl http://localhost:8080/api/salud
   ```

3. **Llevar el puerto del backend al dispositivo y ejecutar.** La app apunta a `http://localhost:8080/` (`ClienteRed.BASE_URL`) y el puerto se reenvía por adb. Funciona igual en emulador que en un móvil por USB, sin depender de la IP del ordenador ni del cortafuegos:

   ```bash
   adb reverse tcp:8080 tcp:8080    # repetir tras reiniciar el emulador o adb
   ./gradlew installDebug
   ```

> **Antes de la P3.2** hay que deshacer este atajo: volver a `http://10.0.2.2:8080/` en el emulador, o a la IP del ordenador en un móvil físico. Los proxys excluyen `localhost`, así que con este atajo no verían el tráfico. También se quitará `android:usesCleartextTraffic="true"` del manifiesto, que hoy está a propósito.

## Qué hay en la app (cierre del Bloque II)

- Inicio de sesión, listado «Mis entradas» con copia local en Room y detalle de cada entrada. El modelo del backend común (`Elemento`) se llama aquí `Entrada`; el JSON se mapea con `@SerialName`.
- Cierre de sesión que borra la copia local del usuario antes de vaciar la sesión (corrección de la P2.2, sección 5.6).
- Enlace profundo `gymit://entrada/{id}`, permiso `CAMERA` pedido solo cuando se necesita, y `FLAG_SECURE` en la ventana.
- **Deliberadamente insegura** hasta el Bloque III: token en claro en `shared_prefs`, base de datos sin cifrar, registros con datos sensibles y tráfico HTTP.

El inventario de datos sensibles, la superficie expuesta, el modelo de amenazas y el reparto están en **[docs/ALCANCE.md](docs/ALCANCE.md)**, el documento de alcance del Bloque II. El estado entregado se marca con el tag `bloque-2`.

## Reconocimiento de una aplicación real (apartado 5.4)

**Aplicación analizada:** Basic-Fit · **Paquete:** `com.basicfit.trainingApp` · **Versión:** 1.98.2 (versionCode 2987).
Se extrajo con `adb pull` de un Pixel 9 Pro con Android 17, el 27/10/2026.

Es la app real más parecida a Gymit: el socio enseña en el torno un QR generado en el móvil para entrar en el gimnasio.

| Qué mirar | Con qué | Observación |
|---|---|---|
| Permisos declarados | `aapt2 dump permissions` | **36 permisos solicitados**. Los peligrosos son `CAMERA`, `RECORD_AUDIO`, `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`, `ACTIVITY_RECOGNITION`, `READ_EXTERNAL_STORAGE` y `WRITE_EXTERNAL_STORAGE`, y cuatro de Health Connect (pasos y calorías). Otros relevantes: `NFC`, `USE_BIOMETRIC`, `DETECT_SCREEN_CAPTURE`, `WRITE_SETTINGS`, `ACCESS_NOTIFICATION_POLICY`, y `AD_ID` y `ACCESS_ADSERVICES_*` para publicidad. |
| Componentes exportados | `aapt2 dump xmltree` sobre el manifiesto (equivale a `apktool`) | Tiene **14 componentes exportados** de 83 (5 activities, 1 alias, 3 servicios y 5 receivers). **7 no exigen ningún permiso**: `MainActivity`, con deep links `https` hacia `pages.basic-fit.com` y `redirect.basic-fit.com` y el esquema propio `bfa://`; `PermissionsRationaleActivity`; `RedirectUriReceiverActivity` (retorno del login OAuth, esquema `com.basicfit.trainingapp://`); `CropImageActivity` y `NotificationReceiverActivity`, **exportadas sin `intent-filter`**; `HealthDataSdkService`; y `AlarmPermissionBroadcastReceiver`. Tiene `allowBackup=false` y `usesCleartextTraffic=false`. |
| Esquemas de firma | `apksigner verify --print-certs -v` | **Firmas v2 y v3**, más una SourceStamp de Google Play. La clave es **RSA de 4096 bits** y el certificado es de Google (Play App Signing): Google custodia la clave de firma, no Basic-Fit. |
| `minSdk` y `targetSdk` | `aapt2 dump badging` | `minSdk` 26 (Android 8.0), `targetSdk` 36 (Android 16) y `compileSdk` 36. |
| Número de piezas instaladas | `adb shell pm path` | **5 piezas** (App Bundle): `base.apk`, `split_config.arm64_v8a.apk` (librerías nativas), `split_config.en.apk` y `split_config.es.apk` (idiomas) y `split_config.xxhdpi.apk` (densidad de pantalla). |

**Notas para el cuaderno del equipo**

- **Permisos difíciles de justificar** para una app de acceso y entrenamiento: `RECORD_AUDIO`, `WRITE_SETTINGS`, `ACCESS_NOTIFICATION_POLICY`, `WRITE_EXTERNAL_STORAGE` sin límite de versión y los identificadores de publicidad. `CAMERA` se puede justificar, porque la app incluye VisionCamera y un recortador de imágenes para la foto de perfil. Biometría y `NFC` encajan con el acceso al club.
- **Componentes exportados:** 14, y 7 sin permiso. Los que más llaman la atención son `CropImageActivity` (librería `com.canhub.cropper`) y `NotificationReceiverActivity` (notifee): están exportadas sin `intent-filter`, así que cualquier app puede lanzarlas con `am start -n`, como hicimos en el paso 10, sin que tengan que estar expuestas. La otra superficie importante son los deep links y el retorno de OAuth.
- **Configuración de red:** tráfico en claro prohibido en general, pero **permitido hacia `10.0.2.2` y `localhost`**, restos del entorno de desarrollo que llegaron a producción. Solo confía en certificados de usuario en `debug-overrides` y **no tiene certificate pinning** (no hay `pin-set`).
- **Para la auditoría:** está escrita en **React Native**. La lógica va en `assets/index.android.bundle` compilada a bytecode Hermes, así que jadx solo muestra el envoltorio Java. Usa `libsqlcipher.so` (base de datos local cifrada) y MMKV para almacenamiento, `libtoolChecker.so` (RootBeer) para **detectar root** y `DETECT_SCREEN_CAPTURE` para **detectar capturas de pantalla**. Ese último es un control parecido al `FLAG_SECURE` que tenemos previsto, aunque detectar una captura no es lo mismo que impedirla.
- **Qué nos llevamos para Gymit:**
  - Hacer: cifrar los datos locales, detectar root y usar Play App Signing o una clave RSA de 4096 bits.
  - Evitar: dejar excepciones de tráfico en claro en la versión publicada, exportar actividades sin necesidad, pedir permisos que no usamos y prescindir del pinning.

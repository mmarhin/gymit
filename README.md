# Gymit

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

Gymit es el carné digital del gimnasio: para entrar, el socio enseña en el torno un código QR firmado que cambia cada 30 segundos y que solo se genera en el móvil vinculado a su cuenta, y el servidor comprueba la firma, la caducidad y que el código no se haya usado antes. La app incluye también el alta del socio con un selfie en directo y el historial de sus entradas. El dato más sensible es la credencial de acceso, la clave privada con la que el móvil firma los QR: quien la obtenga puede entrar en el gimnasio haciéndose pasar por el socio.

## Requisitos técnicos mínimos

| Requisito | Cómo lo cumple Gymit | Responsable |
|---|---|---|
| Autenticación de usuario | Login con correo y contraseña contra Supabase Auth, y huella (BiometricPrompt) para desbloquear el pase. La cuenta queda vinculada a un único móvil: el que registró su clave pública al darse de alta. | Iván Mora |
| Almacenamiento local de datos sensibles | La clave privada de firma vive en el Android Keystore, no exportable y ligada a la autenticación biométrica. El perfil, el selfie y el historial de accesos se guardan cifrados en el almacenamiento privado de la app. | Alberto Bartolomé |
| Llamada a una API remota | Supabase: Auth para la sesión, PostgreSQL con Row Level Security para que cada socio solo vea sus datos, y una Edge Function que valida el QR en el torno (firma, caducidad y nonce de un solo uso). | Mario Marín |
| Permiso peligroso justificado | `CAMERA`, usada con CameraX para hacer el selfie en directo durante el alta. Permite comprobar en el torno que quien entra es el titular, y no se acepta una foto subida desde la galería. | Alberto Neumann |

## Convenciones del equipo

| Decisión | Valor |
|---|---|
| Nombre de paquete | `es.uma.seguridad.gymit` |
| `minSdk` | 30 (Android 11) |
| `targetSdk` | 37 (Android 17) |
| Responsables | Los de la tabla de requisitos técnicos mínimos |

**Justificación del `targetSdk`.** Declaramos la API 37 porque es la versión estable más reciente de Android. Con ella el sistema aplica a la aplicación todas las restricciones de seguridad y privacidad vigentes, porque muchas sólo se activan si la app las declara como objetivo. Además supera el mínimo que Google Play exige hoy a las aplicaciones nuevas (API 36). Un `targetSdk` más bajo dejaría activos comportamientos heredados que en una auditoría contarían como hallazgo.

**Ramas y revisión.**
- `main` es la rama estable y siempre compila. No se hace push directo a `main`.
- Cada tarea se hace en su propia rama, que sale de `main`: `feat/<tema>`, `fix/<tema>`, `docs/<tema>`, `chore/<tema>`.
- La integración se hace por Pull Request con **al menos una revisión aprobada** de otro miembro, y la rama tiene que compilar (`./gradlew assembleDebug`) antes de fusionarse.
- Los mensajes de commit siguen [Conventional Commits](https://www.conventionalcommits.org/es/): `feat: …`, `fix: …`, `docs: …`, `chore: …`.

## Cómo compilar y ejecutar

Hace falta Android Studio con el SDK y un JDK 17 o superior (el de Android Studio sirve).

1. **Clonar y compilar.**

   ```bash
   git clone git@github.com:mmarhin/gymit.git && cd gymit
   ./gradlew assembleDebug
   ```

2. **Arrancar el backend de la asignatura.** No está en este repositorio: es el material común del campus virtual (`Material comun backend y proyecto base.zip`, carpeta `backend/`), y los usuarios de prueba están en su `LEEME.md`.

   ```bash
   python3 servidor.py          # o: java Servidor.java
   ```

3. **Llevar el puerto del backend al dispositivo e instalar.** La app apunta a `http://localhost:8080/` y el puerto se reenvía por adb, en emulador o en un móvil por USB:

   ```bash
   adb reverse tcp:8080 tcp:8080
   ./gradlew installDebug       # instala la app del socio y la del torno
   ```

## Reconocimiento de una aplicación real (apartado 5.4)

**Aplicación analizada:** Basic-Fit · **Paquete:** `com.basicfit.trainingApp` · **Versión:** 1.98.2 (versionCode 2987).
Se extrajo con `adb pull` de un Pixel 9 Pro con Android 17, el 27/10/2026.

| Qué mirar | Con qué | Observación |
|---|---|---|
| Permisos declarados | `aapt2 dump permissions` | **36 permisos solicitados**. Los peligrosos son `CAMERA`, `RECORD_AUDIO`, `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`, `ACTIVITY_RECOGNITION`, `READ_EXTERNAL_STORAGE` y `WRITE_EXTERNAL_STORAGE`, y cuatro de Health Connect (pasos y calorías). Otros relevantes: `NFC`, `USE_BIOMETRIC`, `DETECT_SCREEN_CAPTURE`, `WRITE_SETTINGS`, `ACCESS_NOTIFICATION_POLICY`, y `AD_ID` y `ACCESS_ADSERVICES_*` para publicidad. |
| Componentes exportados | `aapt2 dump xmltree` sobre el manifiesto (equivale a `apktool`) | Tiene **14 componentes exportados** de 83 (5 activities, 1 alias, 3 servicios y 5 receivers). **7 no exigen ningún permiso**: `MainActivity`, con deep links `https` hacia `pages.basic-fit.com` y `redirect.basic-fit.com` y el esquema propio `bfa://`; `PermissionsRationaleActivity`; `RedirectUriReceiverActivity` (retorno del login OAuth, esquema `com.basicfit.trainingapp://`); `CropImageActivity` y `NotificationReceiverActivity`, **exportadas sin `intent-filter`**; `HealthDataSdkService`; y `AlarmPermissionBroadcastReceiver`. Tiene `allowBackup=false` y `usesCleartextTraffic=false`. |
| Esquemas de firma | `apksigner verify --print-certs -v` | **Firmas v2 y v3**, más una SourceStamp de Google Play. La clave es **RSA de 4096 bits** y el certificado es de Google (Play App Signing): Google custodia la clave de firma, no Basic-Fit. |
| `minSdk` y `targetSdk` | `aapt2 dump badging` | `minSdk` 26 (Android 8.0), `targetSdk` 36 (Android 16) y `compileSdk` 36. |
| Número de piezas instaladas | `adb shell pm path` | **5 piezas** (App Bundle): `base.apk`, `split_config.arm64_v8a.apk` (librerías nativas), `split_config.en.apk` y `split_config.es.apk` (idiomas) y `split_config.xxhdpi.apk` (densidad de pantalla). |

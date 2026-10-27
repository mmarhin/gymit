# Gymit · Documento de alcance del proyecto (cierre del Bloque II)

**Grupo K · GymIt** — Alberto Bartolomé Iruela, Mario Marín Hinojosa, Iván Mora Gómez y Alberto Neumann García.
Seguridad en Entornos Móviles · Grado en Ciberseguridad e IA · ETSII · UMA · curso 2026-27.

Este documento describe la aplicación tal como queda al cerrar el Bloque II. Corresponde al tag `bloque-2` del repositorio, que es la versión que se protegerá en el Bloque III. Está escrito para que lo entienda alguien de fuera del equipo, en particular el grupo que nos auditará en el Bloque IV.

## 1. La aplicación

**Dominio:** Acceso, es decir, control de acceso a instalaciones.

**Para qué sirve.** Gymit es el carné digital de un gimnasio. Para pasar el torno, el socio enseña en su móvil un código QR firmado que cambia cada 30 segundos y que solo se puede generar en el móvil vinculado a su cuenta. El servidor comprueba la firma, que el código no haya caducado y que no se haya usado antes, y concede o deniega la entrada. La app incluye además el alta del socio con un selfie hecho en directo y el historial de sus entradas.

**Quién la usa.** La usan los socios del gimnasio. El personal de recepción, o el propio torno, usa otra aplicación aparte que lee el QR y muestra verde o rojo.

**Estado al cerrar el Bloque II.** La aplicación funciona contra el backend común de la asignatura y tiene:
- inicio de sesión;
- listado de entradas, con copia local en Room;
- detalle de cada entrada;
- un enlace profundo `gymit://entrada/{id}`;
- el permiso de cámara, pedido en el momento en que se necesita;
- `FLAG_SECURE` en la ventana.

Por decisión de la asignatura es **deliberadamente insegura**: guarda el token en claro, registra datos sensibles en logcat y se comunica por HTTP. El QR firmado, la clave en el Keystore y la migración a Supabase son el trabajo del Bloque III.

## 2. Datos que maneja

Las tres primeras columnas describen la situación **hoy** y están medidas en un Pixel 9 Pro con Android 17 durante las prácticas P2.2 y P2.3. La última es el plan para el Bloque III.

| Dato | Dónde se guarda hoy | Quién puede leerlo hoy | Cómo se protegerá (Bloque III) |
|---|---|---|---|
| Token de sesión | `shared_prefs/sesion.xml`, en claro. También aparece en logcat, porque `Log.d` imprime el `toString()` de `RespuestaLogin`, y en la cabecera `Authorization` de cada petición HTTP | Cualquiera con adb y la depuración activada (`run-as`, `logcat`); cualquiera en el camino de red; las copias de seguridad, porque `allowBackup=true` | Sesión gestionada con Supabase Auth; almacenamiento cifrado con una clave del Keystore; quitar el `Log.d`; TLS |
| Usuario y contraseña | No se guardan, pero viajan en claro por HTTP (`POST /api/login`) y el interceptor `BODY` los escribe en logcat | Cualquiera en el camino de red; cualquiera con `adb logcat` | TLS con anclaje de certificado; interceptor desactivado en la versión publicada |
| Nombre de usuario | `shared_prefs/sesion.xml`, en claro (se borra al cerrar sesión) | Cualquiera con adb y depuración | Preferencias cifradas |
| Historial de entradas (copia local) | `databases/gymit.db` más el `-wal` (Room, SQLite sin cifrar), con el usuario en la columna `propietario`. Se borra al cerrar sesión desde la corrección de la P2.2 | Cualquiera con adb y depuración (`run-as`), y las copias de seguridad | SQLCipher o cifrado de campos con clave del Keystore; `allowBackup=false` |
| Perfil (nombre, correo, DNI) | Solo en el servidor (`GET /api/perfil`); hoy viajaría en claro | Cualquiera en el camino de red; cualquiera con un token válido | TLS; RLS en Supabase para que cada socio solo lea lo suyo |
| Contenido mostrado en pantalla (QR y datos) | En la miniatura de recientes y en las capturas | Nadie: `FLAG_SECURE` ya está activo (comprobado en la P2.3) | Mantener `FLAG_SECURE` en la pantalla del pase |
| **Credencial de acceso: la clave privada que firma los QR** (el dato más sensible) | Todavía no existe en el código | — | Generada en el Android Keystore, no exportable, ligada a `BIOMETRIC_STRONG`, con Key Attestation al registrarla |
| Selfie del alta | Todavía no existe en el código | — | Cifrado en el almacenamiento privado; subido por TLS; nunca a la galería |
| Dirección del backend | Escrita en el APK (`ClienteRed.BASE_URL`) | Cualquiera que decompile el APK | No es secreta. Nunca se meterán claves en el código |

## 3. Superficie expuesta

| Vía de entrada | Cómo se invoca hoy | Qué comprueba hoy | Se trata en |
|---|---|---|---|
| Activity principal exportada | `adb shell am start -n es.uma.seguridad.gymit/.MainActivity` | Nada (es el lanzador, tiene que estar exportada) | Sesión 3.4 |
| Enlace profundo `gymit://entrada/{id}` | `adb shell am start -a android.intent.action.VIEW -d "gymit://entrada/2" es.uma.seguridad.gymit`, o un enlace web, un mensaje u otra app | Nada en la interfaz: la pantalla se abre siempre. Los datos solo llegan si hay token (lo comprueba el repositorio) | Sesión 3.4 |
| Activities de depuración `PreviewActivity` y `ComponentActivity` (vienen de las librerías de tooling y test de Compose) | `adb shell am start -n es.uma.seguridad.gymit/androidx.compose.ui.tooling.PreviewActivity` | Nada. Solo están en el APK de depuración | Sesión 3.4 (comprobar que no llegan a release) |
| Llamadas a la API | `curl` con un token válido | Solo que el token exista. `GET /api/elementos/{id}` devuelve elementos de otros usuarios (IDOR) y `/api/admin/usuarios` responde a un usuario normal | Sesión 3.5 · P3.2 (el canal) · Bloque IV (la autorización); RLS en Supabase |
| Tráfico de red | Cualquiera en la misma red (HTTP, `usesCleartextTraffic=true`) | Nada | Sesión 3.5 · P3.2 |
| Almacenamiento local | `adb shell run-as es.uma.seguridad.gymit` (la app es depurable) | Nada: está en claro | Sesión 3.3 · P3.1 |
| Copias de seguridad | `allowBackup=true`: copia en la nube o transferencia entre dispositivos | Nada | Sesión 3.3 · P3.3 |
| Registros | `adb logcat` | Nada: token, credenciales y cuerpos de respuesta en claro | Sesión 3.6 |
| Vista de recientes y capturas | Botón de recientes del sistema, capturas de pantalla | `FLAG_SECURE` activo: miniatura en blanco y captura en negro | Sesión 3.6 |
| Permiso `CAMERA` | Diálogo del sistema, pedido solo al pulsar «Conceder permiso» | Si se deniega, la app sigue funcionando sin el selfie; Android deja de preguntar tras dos negativas | Bloque III (CameraX y selfie) |

## 4. Modelo de amenazas preliminar

1. **El socio que presta o revende su acceso.** Le pasa a un amigo una captura del QR o el propio móvil para que entre sin pagar. Busca que el QR sirva en otro dispositivo o a otra hora. Lo frenan el QR que caduca a los 30 segundos, los nonces de un solo uso, la clave ligada a ese móvil y a su huella, y `FLAG_SECURE`.
2. **Alguien con acceso físico al móvil.** Una pareja, un compañero de piso o quien encuentra el móvil desbloqueado. Busca leer el historial (a qué hora entra y sale el socio), el selfie o el token para hacerse pasar por él. Lo frenan el cifrado local, la clave en el Keystore, `allowBackup=false` y que la versión publicada no sea depurable.
3. **Un atacante en la wifi del gimnasio.** Intercepta el inicio de sesión para robar credenciales o tokens, o modifica respuestas. Lo frenan TLS y el anclaje de certificado.
4. **Un usuario curioso o malicioso contra la API.** Con su propio token pide datos de otros socios o funciones de administración, o fabrica tokens predecibles. Lo frena la autorización en el servidor (RLS en Supabase y la Edge Function), nunca la interfaz.
5. **Una app maliciosa instalada en el mismo móvil.** Lanza el enlace profundo o los componentes exportados para abrir pantallas internas o mandar datos manipulados. Lo frenan validar todo lo que entra por un Intent y exportar solo lo imprescindible.

## 5. Reparto de trabajo

| Requisito técnico mínimo | Responsable |
|---|---|
| Autenticación de usuario (inicio de sesión y huella, cuenta vinculada a un móvil) | Iván Mora Gómez |
| Almacenamiento local de datos sensibles (Keystore, perfil, selfie e historial cifrados) | Alberto Bartolomé Iruela |
| Llamada a una API remota (Supabase: Auth, RLS y Edge Function) | Mario Marín Hinojosa |
| Permiso peligroso justificado (`CAMERA` para el selfie con CameraX) | Alberto Neumann García |

**Justificación del permiso ante el usuario:** «Gymit necesita la cámara para hacerte un selfie al darte de alta, y así el gimnasio puede comprobar que quien entra eres tú». Si se deniega, no se puede completar el alta con foto, pero el inicio de sesión, el historial y el resto de la app siguen funcionando.

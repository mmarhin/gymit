# Cuaderno del equipo · Reconocimiento de Basic-Fit

Notas del apartado 5.4 de la P2.1 («Para el cuaderno del equipo»), sobre la tabla de reconocimiento del README.

- **Permisos difíciles de justificar** para una app de acceso y entrenamiento: `RECORD_AUDIO`, `WRITE_SETTINGS`, `ACCESS_NOTIFICATION_POLICY`, `WRITE_EXTERNAL_STORAGE` sin límite de versión y los identificadores de publicidad. `CAMERA` se puede justificar, porque la app incluye VisionCamera y un recortador de imágenes para la foto de perfil. Biometría y `NFC` encajan con el acceso al club.
- **Componentes exportados:** 14, y 7 sin permiso. Los que más llaman la atención son `CropImageActivity` (librería `com.canhub.cropper`) y `NotificationReceiverActivity` (notifee): están exportadas sin `intent-filter`, así que cualquier app puede lanzarlas con `am start -n`, como hicimos en el paso 10, sin que tengan que estar expuestas. La otra superficie importante son los deep links y el retorno de OAuth.
- **Configuración de red:** tráfico en claro prohibido en general, pero **permitido hacia `10.0.2.2` y `localhost`**, restos del entorno de desarrollo que llegaron a producción. Solo confía en certificados de usuario en `debug-overrides` y **no tiene certificate pinning** (no hay `pin-set`).
- **Para la auditoría:** está escrita en **React Native**. La lógica va en `assets/index.android.bundle` compilada a bytecode Hermes, así que jadx solo muestra el envoltorio Java. Usa `libsqlcipher.so` (base de datos local cifrada) y MMKV para almacenamiento, `libtoolChecker.so` (RootBeer) para **detectar root** y `DETECT_SCREEN_CAPTURE` para **detectar capturas de pantalla**. Ese último es un control parecido al `FLAG_SECURE` que tenemos previsto, aunque detectar una captura no es lo mismo que impedirla.
- **Qué nos llevamos para Gymit:**
  - Hacer: cifrar los datos locales, detectar root y usar Play App Signing o una clave RSA de 4096 bits.
  - Evitar: dejar excepciones de tráfico en claro en la versión publicada, exportar actividades sin necesidad, pedir permisos que no usamos y prescindir del pinning.

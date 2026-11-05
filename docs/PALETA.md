# Gymit · Paleta de colores

**Grupo K · GymIt** — Seguridad en Entornos Móviles · ETSII · UMA · curso 2026-27.

Este documento define los colores de la aplicación: de dónde salen, qué papel tiene cada uno y qué contraste se ha medido. El código está en `app/src/main/java/es/uma/seguridad/gymit/ui/theme/` (`Color.kt` tiene las escalas y `Theme.kt` asigna los papeles).

## 1. Idea

Gymit es el carné digital de un gimnasio: el socio enseña un QR en el torno y el torno contesta verde o rojo. La paleta sale de ahí:

| Color | Significado | De dónde sale |
|---|---|---|
| **Índigo** `#4949DD` | La marca y las acciones. Transmite confianza y tecnología, que es lo que tiene que inspirar una app que guarda tu credencial de acceso. | El fondo del logo |
| **Menta** `#69F2AF` | Acceso concedido: «puedes pasar». | La línea de lectura del logo |
| **Rojo** `#BA172A` | Acceso denegado y errores. | Elegido para el torno |
| **Ámbar** `#845500` | Avisos (el pase está a punto de caducar, datos sin conexión). | Elegido para avisos |
| **Neutros** | Fondos, texto, bordes. Grises con un toque del índigo para que todo case. | Derivados del índigo |

Estos colores son también los que verá el socio en el torno, así que **no se usan para otra cosa**: nada decorativo en verde ni en rojo.

## 2. Método

Cada color es una **escala tonal** como las de Material 3: se mantiene el matiz del color ancla y se varía el **tono**, que es la luminosidad percibida (L\* de CIELAB, de 0 = negro a 100 = blanco). Así el contraste se controla con números: dos tonos separados 50 o más cumplen siempre el 4,5:1 de WCAG AA para texto.

El índigo del logo tiene exactamente **tono 40**, el que Material 3 usa para el color principal en modo claro, así que se ha usado tal cual. El menta del logo tiene **tono 87**: luce sobre índigo y sobre fondo oscuro, pero sobre blanco da **1,4:1** y no se lee. Para texto en verde sobre fondo claro se usa su tono 40.

## 3. Escalas tonales

| Tono | 10 | 20 | 30 | 40 | 50 | 60 | 70 | 80 | 87 | 90 | 95 | 98 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Índigo** | `#160065` | `#2800A0` | `#372AC0` | `#4949DD` | `#5D65FA` | `#7786FF` | `#96A5FF` | `#B7C3FF` | `#D0D8FF` | `#DBE2FF` | `#EDF1FF` | `#F7F9FF` |
| **Menta** | `#002111` | `#003821` | `#005232` | `#006D44` | `#008857` | `#00A66B` | `#2DC282` | `#53DE9D` | `#69F2AF` | `#73FCB8` | `#BEFFDA` | `#E7FFF1` |
| **Rojo** | `#400006` | `#680010` | `#93001A` | `#BA172A` | `#D93C44` | `#F85A5D` | `#FF8885` | `#FFB4AF` | `#FFCFCB` | `#FFD9D7` | `#FFEDEB` | `#FFF7F7` |
| **Ámbar** | `#2A1700` | `#462B00` | `#643F00` | `#845500` | `#A56B00` | `#C78300` | `#EA9A0A` | `#FFB959` | `#FFD29B` | `#FFDDB4` | `#FFEFDB` | `#FFF9F1` |
| **Neutro** | `#1A1B22` | `#2F3037` | `#45464E` | `#5C5E66` | `#75767E` | `#8F9199` | `#A9ABB3` | `#C4C7CF` | `#D7D9E2` | `#E0E3EB` | `#EEF1F9` | `#F7F9FF` |
| **Neutro variante** | `#181B2A` | `#2C3040` | `#424657` | `#595E70` | `#72768A` | `#8B90A4` | `#A6AABF` | `#C1C6DB` | `#D4DAEF` | `#DCE1F7` | `#EDF0FF` | `#F7F9FF` |

En el código: `Indigo[40]`, `Menta[87]`, `Neutro[98]`…

## 4. Papeles

Las pantallas nunca usan un color suelto: piden un papel. Así funcionan igual en modo claro y en modo oscuro.

### 4.1 Material 3 (`MaterialTheme.colorScheme`)

| Papel | Claro | Oscuro | Para qué |
|---|---|---|---|
| `primary` | `#4949DD` | `#B7C3FF` | Botones, pestaña activa, enlaces |
| `onPrimary` | `#FFFFFF` | `#2800A0` | Texto sobre primary |
| `primaryContainer` | `#DBE2FF` | `#372AC0` | Fondos suaves índigo (casillas de fecha, iconos) |
| `onPrimaryContainer` | `#372AC0` | `#DBE2FF` | Texto sobre primaryContainer |
| `secondary` | `#006D44` | `#53DE9D` | Iconos y textos en verde |
| `secondaryContainer` | `#BEFFDA` | `#005232` | Aviso de «Completa tu alta», etiqueta de rol |
| `onSecondaryContainer` | `#003821` | `#73FCB8` | Texto sobre secondaryContainer |
| `error` | `#BA172A` | `#FFB4AF` | Mensajes de error |
| `errorContainer` | `#FFD9D7` | `#93001A` | Caja de error |
| `onErrorContainer` | `#93001A` | `#FFD9D7` | Texto en la caja de error |
| `background` | `#F7F9FF` | `#111319` | Fondo de las pantallas |
| `onSurface` | `#1A1B22` | `#E0E3EB` | Texto principal |
| `onSurfaceVariant` | `#424657` | `#C1C6DB` | Texto secundario |
| `outline` | `#72768A` | `#8B90A4` | Bordes de los campos |
| `outlineVariant` | `#C1C6DB` | `#424657` | Separadores |

### 4.2 Propios de Gymit (`GymitTema.colores`)

Material 3 no tiene papeles para «acceso concedido» o «denegado», así que se añaden aparte.

| Papel | Claro | Oscuro | Para qué |
|---|---|---|---|
| `marca` | `#4949DD` | `#4949DD` | Cabecera del acceso y tarjeta del pase. Es el índigo del logo **en los dos modos**: en oscuro, `primary` pasa a lavanda y el pase dejaría de parecerse al logo |
| `onMarca` | `#FFFFFF` | `#FFFFFF` | Texto sobre la marca |
| `marcaAcento` | `#69F2AF` | `#69F2AF` | El menta del logo, sólo sobre la marca |
| `tarjeta` | `#FFFFFF` | `#1E1F26` | Fondo de las tarjetas |
| `acceso` | `#69F2AF` | `#69F2AF` | Pantalla verde del torno, etiqueta «Activo» |
| `onAcceso` | `#002111` | `#002111` | Texto sobre acceso |
| `accesoTexto` | `#006D44` | `#53DE9D` | «Acceso concedido» escrito sobre el fondo normal |
| `denegado` | `#BA172A` | `#FFB4AF` | Pantalla roja del torno |
| `onDenegado` | `#FFFFFF` | `#680010` | Texto sobre denegado |
| `aviso` | `#845500` | `#FFB959` | Icono de aviso |
| `avisoContenedor` | `#FFDDB4` | `#643F00` | Caja de aviso |
| `onAvisoContenedor` | `#462B00` | `#FFDDB4` | Texto en la caja de aviso |
| `qrModulo` | `#0F1324` | `#0F1324` | Módulos del QR |
| `qrFondo` | `#FFFFFF` | `#FFFFFF` | Fondo del QR (siempre blanco, también en oscuro) |

## 5. Reglas de uso

1. **Verde = puedes pasar, rojo = no.** El menta y el rojo se reservan para el estado del acceso y los errores. Si algo es sólo decorativo, va en índigo o en neutro.
2. **El menta del logo (`#69F2AF`) nunca va sobre fondo claro.** Sobre blanco no se lee. Sobre fondo claro, el verde es `Menta[40]` (`#006D44`).
3. **La tarjeta del pase siempre es índigo de marca**, también en modo oscuro, para que el socio la reconozca de un vistazo en el torno.
4. **El QR siempre es casi negro sobre blanco puro**, también en modo oscuro. Los lectores de los tornos esperan módulos oscuros sobre fondo claro, y un QR invertido falla en muchos de ellos.
5. **Nada de color dinámico de Android 12+.** La app tiene que verse con los colores de la marca, no con los del fondo de pantalla del socio.
6. **Un color nuevo se añade aquí primero**: como escala en `Color.kt`, como papel en `Theme.kt` y con su contraste medido en la tabla de abajo.

## 6. Contrastes medidos

Contraste calculado con la fórmula de WCAG 2.2 (luminancia relativa). Mínimos: 4,5:1 para texto (AA), 7:1 para el texto principal y el torno (AAA), 3:1 para iconos y bordes.

| Uso | Primer plano / fondo | Mínimo | Claro | Oscuro |
|---|---|---|---|---|
| Texto en botones | `onPrimary` / `primary` | 4,5:1 | 6,40:1 | 7,68:1 |
| Texto en contenedor índigo | `onPrimaryContainer` / `primaryContainer` | 4,5:1 | 7,24:1 | 7,24:1 |
| Enlaces e iconos índigo | `primary` / `background` | 4,5:1 | 6,08:1 | 10,84:1 |
| Iconos y texto en verde | `secondary` / `background` | 4,5:1 | 6,10:1 | 10,88:1 |
| Aviso de alta | `onSecondaryContainer` / `secondaryContainer` | 4,5:1 | 11,66:1 | 7,25:1 |
| Texto principal | `onSurface` / `background` | 7:1 | 16,30:1 | 14,46:1 |
| Texto secundario | `onSurfaceVariant` / `background` | 4,5:1 | 8,88:1 | 10,94:1 |
| Texto secundario en tarjeta | `onSurfaceVariant` / `tarjeta` | 4,5:1 | 9,35:1 | 9,67:1 |
| Texto en tarjeta | `onSurface` / `tarjeta` | 7:1 | 17,16:1 | 12,79:1 |
| Bordes de campos | `outline` / `background` | 3:1 | 4,27:1 | 5,86:1 |
| Mensajes de error | `error` / `background` | 4,5:1 | 6,15:1 | 10,96:1 |
| Caja de error | `onErrorContainer` / `errorContainer` | 4,5:1 | 7,16:1 | 7,16:1 |
| Texto blanco sobre el pase | `onMarca` / `marca` | 4,5:1 | 6,40:1 | 6,40:1 |
| Menta sobre el pase | `marcaAcento` / `marca` | 4,5:1 | 4,55:1 | 4,55:1 |
| Torno: acceso concedido | `onAcceso` / `acceso` | 7:1 | 12,17:1 | 12,17:1 |
| «Acceso concedido» en texto | `accesoTexto` / `background` | 4,5:1 | 6,10:1 | 10,88:1 |
| Torno: denegado | `onDenegado` / `denegado` | 4,5:1 | 6,47:1 | 7,77:1 |
| Icono de aviso | `aviso` / `background` | 3:1 | 6,08:1 | 10,91:1 |
| Caja de aviso | `onAvisoContenedor` / `avisoContenedor` | 4,5:1 | 10,12:1 | 7,21:1 |
| QR | `qrModulo` / `qrFondo` | 15:1 | 18,44:1 | 18,44:1 |

Las 40 combinaciones cumplen su mínimo. El margen más justo es el menta sobre el índigo del pase (4,55:1): por eso ahí el menta sólo se usa en texto grande o en la barra de la cuenta atrás, y la etiqueta «Activo» va en una pastilla menta con texto oscuro (12,17:1).

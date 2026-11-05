package es.uma.seguridad.gymit.ui.theme

import androidx.compose.ui.graphics.Color

/* ==========================================================================
   PALETA DE GYMIT

   Seis escalas tonales con el mismo método que Material 3: cada escala
   mantiene el matiz de su color ancla y varía el tono, que es la luminosidad
   percibida (L* de CIELAB, de 0 = negro a 100 = blanco). El tono decide el
   contraste: dos colores separados 50 tonos o más cumplen siempre el 4,5:1
   que pide WCAG AA para texto.

   Los colores no se usan directamente en las pantallas: el tema (Theme.kt)
   los asigna a papeles (primary, tarjeta, acceso, denegado...) y las
   pantallas piden el papel. La tabla completa, con los contrastes medidos,
   está en docs/PALETA.md.
   ========================================================================== */

/** Una escala tonal: `Indigo[40]` es el índigo de tono 40. */
class PaletaTonal(private val tonos: Map<Int, Color>) {
    operator fun get(tono: Int): Color = tonos.getValue(tono)
}


/** Marca. Ancla: tono 40 = #4949DD, el índigo del logo. */
val Indigo = PaletaTonal(
    mapOf(
        0 to Color(0xFF000000),
        4 to Color(0xFF0A0040),
        6 to Color(0xFF0F004F),
        10 to Color(0xFF160065),
        12 to Color(0xFF190072),
        17 to Color(0xFF22008F),
        20 to Color(0xFF2800A0),
        22 to Color(0xFF2B05A9),
        30 to Color(0xFF372AC0),
        40 to Color(0xFF4949DD),
        50 to Color(0xFF5D65FA),
        60 to Color(0xFF7786FF),
        70 to Color(0xFF96A5FF),
        80 to Color(0xFFB7C3FF),
        87 to Color(0xFFD0D8FF),
        90 to Color(0xFFDBE2FF),
        92 to Color(0xFFE2E8FF),
        94 to Color(0xFFE9EDFF),
        95 to Color(0xFFEDF1FF),
        96 to Color(0xFFF1F3FF),
        98 to Color(0xFFF7F9FF),
        100 to Color(0xFFFFFFFF)
    )
)

/** Acceso concedido. Ancla: tono 87 = #69F2AF, la línea de lectura del logo. */
val Menta = PaletaTonal(
    mapOf(
        0 to Color(0xFF000000),
        4 to Color(0xFF001207),
        6 to Color(0xFF00170B),
        10 to Color(0xFF002111),
        12 to Color(0xFF002615),
        17 to Color(0xFF00311D),
        20 to Color(0xFF003821),
        22 to Color(0xFF003E25),
        30 to Color(0xFF005232),
        40 to Color(0xFF006D44),
        50 to Color(0xFF008857),
        60 to Color(0xFF00A66B),
        70 to Color(0xFF2DC282),
        80 to Color(0xFF53DE9D),
        87 to Color(0xFF69F2AF),
        90 to Color(0xFF73FCB8),
        92 to Color(0xFF88FFC2),
        94 to Color(0xFFAEFFD2),
        95 to Color(0xFFBEFFDA),
        96 to Color(0xFFCDFFE2),
        98 to Color(0xFFE7FFF1),
        100 to Color(0xFFFFFFFF)
    )
)

/** Acceso denegado y errores. */
val Rojo = PaletaTonal(
    mapOf(
        0 to Color(0xFF000000),
        4 to Color(0xFF270002),
        6 to Color(0xFF320004),
        10 to Color(0xFF400006),
        12 to Color(0xFF480008),
        17 to Color(0xFF5C000D),
        20 to Color(0xFF680010),
        22 to Color(0xFF710012),
        30 to Color(0xFF93001A),
        40 to Color(0xFFBA172A),
        50 to Color(0xFFD93C44),
        60 to Color(0xFFF85A5D),
        70 to Color(0xFFFF8885),
        80 to Color(0xFFFFB4AF),
        87 to Color(0xFFFFCFCB),
        90 to Color(0xFFFFD9D7),
        92 to Color(0xFFFFE2DF),
        94 to Color(0xFFFFE8E7),
        95 to Color(0xFFFFEDEB),
        96 to Color(0xFFFFF0EF),
        98 to Color(0xFFFFF7F7),
        100 to Color(0xFFFFFFFF)
    )
)

/** Avisos: pase a punto de caducar, datos sin conexión. */
val Ambar = PaletaTonal(
    mapOf(
        0 to Color(0xFF000000),
        4 to Color(0xFF180B00),
        6 to Color(0xFF1F1100),
        10 to Color(0xFF2A1700),
        12 to Color(0xFF2F1B00),
        17 to Color(0xFF3D2500),
        20 to Color(0xFF462B00),
        22 to Color(0xFF4B2F00),
        30 to Color(0xFF643F00),
        40 to Color(0xFF845500),
        50 to Color(0xFFA56B00),
        60 to Color(0xFFC78300),
        70 to Color(0xFFEA9A0A),
        80 to Color(0xFFFFB959),
        87 to Color(0xFFFFD29B),
        90 to Color(0xFFFFDDB4),
        92 to Color(0xFFFFE4C3),
        94 to Color(0xFFFFEBD2),
        95 to Color(0xFFFFEFDB),
        96 to Color(0xFFFFF1E1),
        98 to Color(0xFFFFF9F1),
        100 to Color(0xFFFFFFFF)
    )
)

/** Fondos, superficies y texto. Gris con un toque del matiz índigo. */
val Neutro = PaletaTonal(
    mapOf(
        0 to Color(0xFF000000),
        4 to Color(0xFF0D0E13),
        6 to Color(0xFF111319),
        10 to Color(0xFF1A1B22),
        12 to Color(0xFF1E1F26),
        17 to Color(0xFF282A30),
        20 to Color(0xFF2F3037),
        22 to Color(0xFF33353B),
        30 to Color(0xFF45464E),
        40 to Color(0xFF5C5E66),
        50 to Color(0xFF75767E),
        60 to Color(0xFF8F9199),
        70 to Color(0xFFA9ABB3),
        80 to Color(0xFFC4C7CF),
        87 to Color(0xFFD7D9E2),
        90 to Color(0xFFE0E3EB),
        92 to Color(0xFFE6E8F0),
        94 to Color(0xFFEBEEF6),
        95 to Color(0xFFEEF1F9),
        96 to Color(0xFFF1F3FC),
        98 to Color(0xFFF7F9FF),
        100 to Color(0xFFFFFFFF)
    )
)

/** Texto secundario, bordes y separadores. Algo más de índigo que el neutro. */
val NeutroVariante = PaletaTonal(
    mapOf(
        0 to Color(0xFF000000),
        4 to Color(0xFF0B0D1C),
        6 to Color(0xFF101221),
        10 to Color(0xFF181B2A),
        12 to Color(0xFF1B1E2E),
        17 to Color(0xFF262939),
        20 to Color(0xFF2C3040),
        22 to Color(0xFF313445),
        30 to Color(0xFF424657),
        40 to Color(0xFF595E70),
        50 to Color(0xFF72768A),
        60 to Color(0xFF8B90A4),
        70 to Color(0xFFA6AABF),
        80 to Color(0xFFC1C6DB),
        87 to Color(0xFFD4DAEF),
        90 to Color(0xFFDCE1F7),
        92 to Color(0xFFE2E8FD),
        94 to Color(0xFFE9EDFF),
        95 to Color(0xFFEDF0FF),
        96 to Color(0xFFF1F3FF),
        98 to Color(0xFFF7F9FF),
        100 to Color(0xFFFFFFFF)
    )
)

/** Los dos colores del logo, tal cual. */
val IndigoLogo = Indigo[40]
val MentaLogo = Menta[87]

/** Módulos del QR: casi negro sobre blanco puro (18,4:1) para que cualquier lector lo lea a la primera. */
val QrModulo = Color(0xFF0F1324)
val QrFondo = Color(0xFFFFFFFF)

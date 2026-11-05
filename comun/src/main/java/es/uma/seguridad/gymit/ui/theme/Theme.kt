package es.uma.seguridad.gymit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/* ==========================================================================
   TEMA DE GYMIT

   Asigna los tonos de Color.kt a papeles. Las pantallas nunca usan un color
   suelto: piden el papel (MaterialTheme.colorScheme.primary,
   GymitTema.colores.acceso...) y así funcionan igual en modo claro y oscuro.

   Reglas de la paleta (detalladas en docs/PALETA.md):
   - Índigo = la marca y las acciones. Menta = acceso concedido. Rojo =
     denegado o error. Ámbar = aviso. Son los mismos colores que verá el
     socio en el torno, así que no se usan para otra cosa.
   - El menta del logo (tono 87) sólo va sobre índigo o sobre fondo oscuro.
     Sobre blanco da 1,4:1 y no se lee: ahí se usa su tono 40.
   ========================================================================== */

private val EsquemaClaro = lightColorScheme(
    primary = Indigo[40],
    onPrimary = Indigo[100],
    primaryContainer = Indigo[90],
    onPrimaryContainer = Indigo[30],
    inversePrimary = Indigo[80],
    secondary = Menta[40],
    onSecondary = Menta[100],
    secondaryContainer = Menta[95],
    onSecondaryContainer = Menta[20],
    tertiary = Menta[40],
    onTertiary = Menta[100],
    tertiaryContainer = Menta[87],
    onTertiaryContainer = Menta[10],
    error = Rojo[40],
    onError = Rojo[100],
    errorContainer = Rojo[90],
    onErrorContainer = Rojo[30],
    background = Neutro[98],
    onBackground = Neutro[10],
    surface = Neutro[98],
    onSurface = Neutro[10],
    surfaceVariant = NeutroVariante[90],
    onSurfaceVariant = NeutroVariante[30],
    surfaceTint = Indigo[40],
    inverseSurface = Neutro[20],
    inverseOnSurface = Neutro[95],
    outline = NeutroVariante[50],
    outlineVariant = NeutroVariante[80],
    scrim = Neutro[0],
    surfaceBright = Neutro[98],
    surfaceDim = Neutro[87],
    surfaceContainerLowest = Neutro[100],
    surfaceContainerLow = Neutro[96],
    surfaceContainer = Neutro[94],
    surfaceContainerHigh = Neutro[92],
    surfaceContainerHighest = Neutro[90]
)

private val EsquemaOscuro = darkColorScheme(
    primary = Indigo[80],
    onPrimary = Indigo[20],
    primaryContainer = Indigo[30],
    onPrimaryContainer = Indigo[90],
    inversePrimary = Indigo[40],
    secondary = Menta[80],
    onSecondary = Menta[20],
    secondaryContainer = Menta[30],
    onSecondaryContainer = Menta[90],
    tertiary = Menta[87],
    onTertiary = Menta[10],
    tertiaryContainer = Menta[30],
    onTertiaryContainer = Menta[90],
    error = Rojo[80],
    onError = Rojo[20],
    errorContainer = Rojo[30],
    onErrorContainer = Rojo[90],
    background = Neutro[6],
    onBackground = Neutro[90],
    surface = Neutro[6],
    onSurface = Neutro[90],
    surfaceVariant = NeutroVariante[30],
    onSurfaceVariant = NeutroVariante[80],
    surfaceTint = Indigo[80],
    inverseSurface = Neutro[90],
    inverseOnSurface = Neutro[20],
    outline = NeutroVariante[60],
    outlineVariant = NeutroVariante[30],
    scrim = Neutro[0],
    surfaceBright = Neutro[22],
    surfaceDim = Neutro[6],
    surfaceContainerLowest = Neutro[4],
    surfaceContainerLow = Neutro[10],
    surfaceContainer = Neutro[12],
    surfaceContainerHigh = Neutro[17],
    surfaceContainerHighest = Neutro[22]
)

/**
 * Papeles propios de Gymit que Material 3 no trae.
 *
 * @property marca Superficies de marca (cabecera del acceso, tarjeta del
 *   pase). Es el índigo del logo en los dos modos: en oscuro, `primary` pasa a
 *   un lavanda claro y la tarjeta del pase dejaría de parecerse al logo.
 * @property marcaAcento El menta del logo, para destacar sobre `marca`.
 * @property tarjeta Fondo de las tarjetas: blanco en claro, un escalón por
 *   encima del fondo en oscuro.
 * @property acceso Acceso concedido (pantalla verde del torno, «Activo»).
 * @property accesoTexto «Acceso concedido» escrito sobre el fondo normal.
 * @property denegado Acceso denegado (pantalla roja del torno). Igual en los dos
 *   modos, como `acceso`: es un fondo a pantalla completa y tiene que leerse
 *   como «rojo» también con el tema oscuro, que es el que usa el torno.
 * @property aviso Icono de aviso, por ejemplo el pase a punto de caducar.
 * @property avisoSobreMarca Aviso sobre la tarjeta índigo del pase: los
 *   últimos segundos del código (Ámbar 87, 4,56:1 sobre el índigo).
 * @property avisoSobreQr Aviso sobre el blanco del QR ampliado, que es blanco
 *   también en modo oscuro (Ámbar 40, 6,40:1).
 */
@Immutable
data class ColoresGymit(
    val marca: Color,
    val onMarca: Color,
    val marcaAcento: Color,
    val tarjeta: Color,
    val acceso: Color,
    val onAcceso: Color,
    val accesoTexto: Color,
    val denegado: Color,
    val onDenegado: Color,
    val denegadoContenedor: Color,
    val onDenegadoContenedor: Color,
    val aviso: Color,
    val avisoContenedor: Color,
    val onAvisoContenedor: Color,
    val avisoSobreMarca: Color,
    val avisoSobreQr: Color,
    val qrModulo: Color,
    val qrFondo: Color
)

private val ColoresClaros = ColoresGymit(
    marca = IndigoLogo,
    onMarca = Indigo[100],
    marcaAcento = MentaLogo,
    tarjeta = Neutro[100],
    acceso = MentaLogo,
    onAcceso = Menta[10],
    accesoTexto = Menta[40],
    denegado = Rojo[40],
    onDenegado = Rojo[100],
    denegadoContenedor = Rojo[90],
    onDenegadoContenedor = Rojo[30],
    aviso = Ambar[40],
    avisoContenedor = Ambar[90],
    onAvisoContenedor = Ambar[20],
    avisoSobreMarca = Ambar[87],
    avisoSobreQr = Ambar[40],
    qrModulo = QrModulo,
    qrFondo = QrFondo
)

private val ColoresOscuros = ColoresClaros.copy(
    tarjeta = Neutro[12],
    accesoTexto = Menta[80],
    denegadoContenedor = Rojo[30],
    onDenegadoContenedor = Rojo[90],
    aviso = Ambar[80],
    avisoContenedor = Ambar[30],
    onAvisoContenedor = Ambar[90]
)

private val LocalColoresGymit = staticCompositionLocalOf { ColoresClaros }

/** Acceso a los papeles propios: `GymitTema.colores.acceso`. */
object GymitTema {
    val colores: ColoresGymit
        @Composable @ReadOnlyComposable get() = LocalColoresGymit.current
}

private val Formas = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Tema de Gymit. No usa el color dinámico de Android 12+ a propósito: la app
 * tiene que verse con los colores de la marca, que son también los del torno.
 */
@Composable
fun GymitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalColoresGymit provides if (darkTheme) ColoresOscuros else ColoresClaros) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            shapes = Formas,
            content = content
        )
    }
}

package es.uma.seguridad.gymit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoNiebla,
    onPrimaryContainer = IndigoProfundo,
    secondary = MentaOscura,
    onSecondary = Color.White,
    secondaryContainer = MentaNiebla,
    onSecondaryContainer = MentaOscura,
    tertiary = Menta,
    onTertiary = Noche,
    background = Nube,
    onBackground = Noche,
    surface = Color.White,
    onSurface = Noche,
    surfaceVariant = IndigoNiebla,
    onSurfaceVariant = Pizarra,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF0F0F8),
    outline = Color(0xFFC9CAD9),
    outlineVariant = Color(0xFFE4E5EF),
    error = Denegado,
    onError = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = IndigoClaro,
    onPrimary = IndigoProfundo,
    primaryContainer = Indigo,
    onPrimaryContainer = Color.White,
    secondary = Menta,
    onSecondary = Noche,
    secondaryContainer = MentaOscura,
    onSecondaryContainer = MentaNiebla,
    tertiary = Menta,
    onTertiary = Noche,
    background = Noche,
    onBackground = Color.White,
    surface = NocheSuperficie,
    onSurface = Color.White,
    surfaceVariant = NocheElevada,
    onSurfaceVariant = Color(0xFFB4B7CC),
    surfaceContainer = NocheSuperficie,
    surfaceContainerHigh = NocheElevada,
    outline = Color(0xFF4A5070),
    outlineVariant = Color(0xFF2E3452),
    error = Color(0xFFFF7A7E),
    onError = Noche
)

private val Formas = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Tema de Gymit. No usa el color dinámico de Android 12+ a propósito: la app
 * tiene que verse con los colores de la marca, que son también los del torno
 * (menta para «adelante», rojo para «denegado»).
 */
@Composable
fun GymitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}

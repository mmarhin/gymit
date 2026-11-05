package es.uma.seguridad.gymit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import es.uma.seguridad.gymit.comun.R

/*
 * Tipografía de Gymit: Manrope, de Google Fonts (licencia SIL Open Font
 * License 1.1, en assets/licencias/OFL-Manrope.txt). Es una fuente variable:
 * un único fichero con todos los pesos, del 200 al 800. Se eligió por sus
 * formas redondeadas, en la línea de la «g» del logo.
 */
private fun manrope(peso: FontWeight) = Font(
    R.font.manrope,
    weight = peso,
    variationSettings = FontVariation.Settings(FontVariation.weight(peso.weight))
)

val Manrope = FontFamily(
    manrope(FontWeight.Normal),
    manrope(FontWeight.Medium),
    manrope(FontWeight.SemiBold),
    manrope(FontWeight.Bold),
    manrope(FontWeight.ExtraBold)
)

private val Base = Typography()

private fun TextStyle.gymit(peso: FontWeight? = null, espaciado: Float? = null) = copy(
    fontFamily = Manrope,
    fontWeight = peso ?: fontWeight,
    letterSpacing = espaciado?.sp ?: letterSpacing
)

val Typography = Typography(
    displayLarge = Base.displayLarge.gymit(FontWeight.Bold, -0.5f),
    displayMedium = Base.displayMedium.gymit(FontWeight.Bold, -0.5f),
    displaySmall = Base.displaySmall.gymit(FontWeight.ExtraBold, -0.5f),
    headlineLarge = Base.headlineLarge.gymit(FontWeight.ExtraBold, -0.5f),
    headlineMedium = Base.headlineMedium.gymit(FontWeight.Bold),
    headlineSmall = Base.headlineSmall.gymit(FontWeight.Bold),
    titleLarge = Base.titleLarge.gymit(FontWeight.Bold),
    titleMedium = Base.titleMedium.gymit(FontWeight.SemiBold),
    titleSmall = Base.titleSmall.gymit(FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.gymit(FontWeight.Medium, 0.1f),
    bodyMedium = Base.bodyMedium.gymit(FontWeight.Medium),
    bodySmall = Base.bodySmall.gymit(FontWeight.Medium),
    labelLarge = Base.labelLarge.gymit(FontWeight.Bold),
    labelMedium = Base.labelMedium.gymit(FontWeight.SemiBold),
    labelSmall = Base.labelSmall.gymit(FontWeight.SemiBold)
)

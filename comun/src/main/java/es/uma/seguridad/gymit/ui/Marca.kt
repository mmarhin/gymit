package es.uma.seguridad.gymit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.uma.seguridad.gymit.comun.R

/* ==========================================================================
   ELEMENTOS DE MARCA

   El logo y el visor de escaneo, compartidos por la app del socio y la del
   torno. Están en el mismo paquete que los componentes de la app del socio
   para que ésta los use sin cambiar ningún import.
   ========================================================================== */

/** El logo de Gymit: el visor con la «g» y la línea de lectura. */
@Composable
fun LogoGymit(modifier: Modifier = Modifier, tamano: Dp = 88.dp) {
    Image(
        painter = painterResource(R.drawable.logo_gymit),
        contentDescription = "Gymit",
        modifier = modifier.size(tamano)
    )
}

/**
 * Dibuja encima del contenido las cuatro esquinas del visor de escaneo, el
 * mismo motivo del logo. Lo usan el pase (alrededor del QR) y la cámara del
 * selfie (alrededor de la cara).
 */
fun Modifier.marcoEscaneo(
    color: Color,
    grosor: Dp = 5.dp,
    largo: Dp = 28.dp,
    radio: Dp = 14.dp
): Modifier = drawWithContent {
    drawContent()
    val g = grosor.toPx()
    val l = largo.toPx()
    val r = radio.toPx()
    val m = g / 2
    val w = size.width - m
    val h = size.height - m
    val trazo = Stroke(width = g, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun esquina(inicio: Offset, vertice: Offset, fin: Offset) {
        val camino = Path().apply {
            moveTo(inicio.x, inicio.y)
            // Una curva cuadrática con el control en el vértice redondea la esquina
            val dx1 = (vertice.x - inicio.x).coerceIn(-1f, 1f)
            val dy1 = (vertice.y - inicio.y).coerceIn(-1f, 1f)
            val dx2 = (fin.x - vertice.x).coerceIn(-1f, 1f)
            val dy2 = (fin.y - vertice.y).coerceIn(-1f, 1f)
            lineTo(vertice.x - dx1 * r, vertice.y - dy1 * r)
            quadraticTo(vertice.x, vertice.y, vertice.x + dx2 * r, vertice.y + dy2 * r)
            lineTo(fin.x, fin.y)
        }
        drawPath(camino, color, style = trazo)
    }
    esquina(Offset(m, m + l), Offset(m, m), Offset(m + l, m))
    esquina(Offset(w - l, m), Offset(w, m), Offset(w, m + l))
    esquina(Offset(m, h - l), Offset(m, h), Offset(m + l, h))
    esquina(Offset(w - l, h), Offset(w, h), Offset(w, h - l))
}


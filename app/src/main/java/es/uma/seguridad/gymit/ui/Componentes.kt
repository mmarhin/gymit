package es.uma.seguridad.gymit.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import es.uma.seguridad.gymit.R
import es.uma.seguridad.gymit.modelo.MotivoError
import es.uma.seguridad.gymit.modelo.Selfie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* ==========================================================================
   PIEZAS COMPARTIDAS DE LA INTERFAZ

   Componentes que usan varias pantallas. Ninguno guarda estado propio ni sabe
   de dónde salen los datos: reciben lo que tienen que dibujar (sesión 2.5,
   elevación de estado).
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

/**
 * Convierte un texto en la imagen de su código QR.
 *
 * Nivel de corrección M: aguanta que se pierda un 15 % del código (un reflejo
 * en la pantalla, un dedo en la esquina) sin hacerlo demasiado denso.
 */
fun generarQr(contenido: String, modulo: Color, fondo: Color, lado: Int = 512): ImageBitmap {
    val matriz = QRCodeWriter().encode(
        contenido,
        BarcodeFormat.QR_CODE,
        lado,
        lado,
        mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 0
        )
    )
    val oscuro = modulo.toArgb()
    val claro = fondo.toArgb()
    val pixeles = IntArray(lado * lado) { i -> if (matriz[i % lado, i / lado]) oscuro else claro }
    return android.graphics.Bitmap.createBitmap(pixeles, lado, lado, android.graphics.Bitmap.Config.ARGB_8888)
        .asImageBitmap()
}

/**
 * Lee el selfie del disco fuera del hilo principal. Vuelve a leerlo cuando
 * cambia la versión, es decir, cuando el socio se hace una foto nueva.
 */
@Composable
fun rememberSelfie(selfie: Selfie?): ImageBitmap? {
    val imagen by produceState<ImageBitmap?>(null, selfie?.ruta, selfie?.version) {
        value = selfie?.let {
            withContext(Dispatchers.IO) { BitmapFactory.decodeFile(it.ruta)?.asImageBitmap() }
        }
    }
    return imagen
}

/** La foto del socio en un círculo, o sus iniciales si todavía no tiene selfie. */
@Composable
fun AvatarSocio(
    selfie: Selfie?,
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 48.dp,
    borde: Color = MaterialTheme.colorScheme.tertiary
) {
    val imagen = rememberSelfie(selfie)
    val forma = Modifier
        .size(tamano)
        .clip(CircleShape)
        .border(width = (tamano.value / 24f).coerceAtLeast(2f).dp, color = borde, shape = CircleShape)
    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = "Foto de $nombre",
            contentScale = ContentScale.Crop,
            modifier = modifier.then(forma)
        )
    } else {
        Box(
            modifier = modifier
                .then(forma)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciales(nombre),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = (tamano.value * 0.36f).sp
            )
        }
    }
}

private fun iniciales(nombre: String): String =
    nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/** Cabecera de las pestañas principales: título grande y acciones a la derecha. */
@Composable
fun CabeceraPestana(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.headlineLarge)
            if (subtitulo != null) {
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        acciones()
    }
}

/** Mensaje a pantalla completa para listas vacías y errores. */
@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (accion != null) {
            Spacer(Modifier.height(16.dp))
            accion()
        }
    }
}

/**
 * Traduce el motivo del error a un texto para la persona que usa la aplicación.
 *
 * El mensaje no revela detalles internos: ni direcciones de servidor, ni rutas,
 * ni trazas de excepciones (sesión 2.4.5).
 */
fun mensajeDeError(motivo: MotivoError): String = when (motivo) {
    MotivoError.SinConexion -> "No se ha podido conectar. Inténtalo de nuevo."
    MotivoError.CredencialesNoValidas -> "Usuario o contraseña incorrectos."
    MotivoError.SesionCaducada -> "La sesión ha caducado. Vuelve a entrar."
    MotivoError.Desconocido -> "Ha ocurrido un error. Inténtalo de nuevo."
}

/** La Activity que hay detrás de un Context de Compose, si la hay. */
tailrec fun Context.buscarActividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActividad()
    else -> null
}

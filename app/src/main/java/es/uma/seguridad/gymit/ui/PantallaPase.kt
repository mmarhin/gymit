package es.uma.seguridad.gymit.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import es.uma.seguridad.gymit.modelo.*
import es.uma.seguridad.gymit.ui.theme.GymitTema

/* ==========================================================================
   EL PASE

   La pantalla que el socio enseña en el torno: su foto, su nombre y un QR que
   cambia cada 30 segundos con su cuenta atrás.

   Igual que el resto de pantallas, no genera nada por su cuenta: recibe el
   pase ya hecho del modelo de vista (que lo pide al repositorio) y se limita a
   dibujarlo.
   ========================================================================== */

@Composable
fun PantallaPase(
    socio: Socio?,
    selfie: Selfie?,
    pase: Pase?,
    selfieComprobado: Boolean,
    onHacerSelfie: () -> Unit
) {
    BrilloMaximo()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CabeceraPestana(
            titulo = "Hola, ${socio?.nombre?.substringBefore(' ') ?: "socio"}",
            subtitulo = "Tu pase para entrar al gimnasio",
            acciones = {
                AvatarSocio(selfie = selfie, nombre = socio?.nombre ?: "", tamano = 48.dp)
                Spacer(Modifier.width(12.dp))
            }
        )

        Spacer(Modifier.height(8.dp))

        TarjetaPase(
            socio = socio,
            selfie = selfie,
            pase = pase,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(20.dp))

        if (selfieComprobado && selfie == null) {
            AvisoAlta(onHacerSelfie, Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.Sensors,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Acerca la pantalla al lector del torno",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TarjetaPase(socio: Socio?, selfie: Selfie?, pase: Pase?, modifier: Modifier = Modifier) {
    val colores = GymitTema.colores
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colores.marca, contentColor = colores.onMarca)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Socio
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                AvatarSocio(selfie = selfie, nombre = socio?.nombre ?: "", tamano = 44.dp, borde = colores.marcaAcento)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        socio?.nombre ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = colores.onMarca
                    )
                    Text(
                        "@${socio?.usuario ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onMarca.copy(alpha = 0.75f)
                    )
                }
                EtiquetaActivo()
            }

            Spacer(Modifier.height(24.dp))

            // QR dentro del visor del logo
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .aspectRatio(1f)
                    .marcoEscaneo(colores.onMarca, grosor = 6.dp, largo = 34.dp, radio = 16.dp)
                    .padding(18.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(colores.qrFondo)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (pase != null) {
                    val qr = remember(pase.contenido) { generarQr(pase.contenido, colores.qrModulo, colores.qrFondo) }
                    Image(
                        bitmap = qr,
                        contentDescription = "Código QR de acceso",
                        filterQuality = FilterQuality.None,
                        modifier = Modifier
                            .fillMaxSize()
                            .lineaDeLectura()
                    )
                } else {
                    CircularProgressIndicator(color = colores.marca)
                }
            }

            Spacer(Modifier.height(22.dp))

            CuentaAtras(pase)
        }
    }
}

/** La barra menta que se vacía en 30 s, como la línea de lectura del logo. */
@Composable
private fun CuentaAtras(pase: Pase?) {
    val colores = GymitTema.colores
    val restante = pase?.segundosRestantes ?: VIGENCIA_PASE_SEGUNDOS
    val fraccion by animateFloatAsState(
        targetValue = restante / VIGENCIA_PASE_SEGUNDOS.toFloat(),
        animationSpec = if (restante == VIGENCIA_PASE_SEGUNDOS) snap() else tween(1000, easing = LinearEasing),
        label = "cuentaAtras"
    )
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Código nuevo en",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onMarca.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f)
            )
            Text(
                "$restante s",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                color = colores.marcaAcento
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(colores.onMarca.copy(alpha = 0.18f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraccion)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(colores.marcaAcento)
            )
        }
    }
}

@Composable
private fun EtiquetaActivo() {
    val colores = GymitTema.colores
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colores.acceso)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(colores.onAcceso)
        )
        Spacer(Modifier.width(6.dp))
        Text("Activo", style = MaterialTheme.typography.labelMedium, color = colores.onAcceso)
    }
}

@Composable
private fun AvisoAlta(onHacerSelfie: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onHacerSelfie),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Completa tu alta",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "Hazte un selfie para que en el torno sepan que eres tú.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/** Una línea menta que recorre el QR de arriba abajo, como un lector leyéndolo. */
private fun Modifier.lineaDeLectura(): Modifier = composed {
    val transicion = rememberInfiniteTransition(label = "lectura")
    val posicion by transicion.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "posicion"
    )
    val color = GymitTema.colores.marcaAcento
    drawWithContent {
        drawContent()
        val alto = 4.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(-6.dp.toPx(), size.height * posicion - alto / 2),
            size = Size(size.width + 12.dp.toPx(), alto),
            cornerRadius = CornerRadius(alto)
        )
    }
}

/**
 * Sube el brillo de la pantalla al máximo mientras se ve el pase, para que el
 * lector del torno lo lea a la primera, y lo devuelve a su valor al salir.
 */
@Composable
private fun BrilloMaximo() {
    val actividad = LocalContext.current.buscarActividad() ?: return
    DisposableEffect(actividad) {
        val ventana = actividad.window
        val anterior = ventana.attributes.screenBrightness
        ventana.attributes = ventana.attributes.apply { screenBrightness = 1f }
        onDispose {
            ventana.attributes = ventana.attributes.apply { screenBrightness = anterior }
        }
    }
}

private tailrec fun Context.buscarActividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActividad()
    else -> null
}

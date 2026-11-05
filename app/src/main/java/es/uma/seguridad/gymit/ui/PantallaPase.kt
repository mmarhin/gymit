package es.uma.seguridad.gymit.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import es.uma.seguridad.gymit.modelo.*
import es.uma.seguridad.gymit.ui.theme.GymitTema

/* ==========================================================================
   EL PASE

   La pantalla que el socio enseña en el torno: un QR que cambia cada 30
   segundos con su cuenta atrás. Al tocar el código se amplía a pantalla
   completa, sobre blanco, para que el lector lo lea mejor.

   Igual que el resto de pantallas, no genera nada por su cuenta: recibe el
   pase ya hecho del modelo de vista (que lo pide al repositorio) y se limita a
   dibujarlo.
   ========================================================================== */

/**
 * @param ampliado si el QR se está enseñando a pantalla completa. Lo guarda la
 *   navegación, porque mientras está ampliado hay que esconder la barra de
 *   pestañas, que no es de esta pantalla.
 */
@Composable
fun PantallaPase(
    socio: Socio?,
    selfie: Selfie?,
    pase: Pase?,
    selfieComprobado: Boolean,
    ampliado: Boolean,
    onAmpliar: (Boolean) -> Unit,
    onHacerSelfie: () -> Unit
) {
    BrilloMaximo()

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CabeceraPestana(
                titulo = "Hola, ${socio?.nombre?.substringBefore(' ') ?: ""}",
                subtitulo = "Tu pase para entrar al gimnasio",
                acciones = {
                    AvatarSocio(selfie = selfie, nombre = socio?.nombre ?: "", tamano = 48.dp)
                    Spacer(Modifier.width(12.dp))
                }
            )

            Spacer(Modifier.height(8.dp))

            TarjetaPase(
                pase = pase,
                onAmpliar = { onAmpliar(true) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(20.dp))

            if (selfieComprobado && selfie == null) {
                AvisoAlta(onHacerSelfie, Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(16.dp))
            }

            Text(
                "Acerca la pantalla al lector del torno.\nToca el código para verlo más grande.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(24.dp))
        }

        /*
         * El QR ampliado es una capa dentro de esta misma pantalla, no un
         * Dialog: un Dialog abre otra ventana, y esa ventana no hereda el
         * FLAG_SECURE de la Activity, así que el QR saldría en las capturas y
         * en la vista de recientes.
         */
        AnimatedVisibility(visible = ampliado, enter = fadeIn(), exit = fadeOut()) {
            QrAmpliado(socio = socio, selfie = selfie, pase = pase, onCerrar = { onAmpliar(false) })
        }
    }
    BackHandler(enabled = ampliado) { onAmpliar(false) }
}

@Composable
private fun TarjetaPase(pase: Pase?, onAmpliar: () -> Unit, modifier: Modifier = Modifier) {
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
            /*
             * El QR va limpio sobre blanco y con todo el ancho de la tarjeta,
             * como en los pases de verdad. El visor del logo se reserva para
             * lo que escanea (el torno y la cámara del selfie), no para lo
             * que se escanea; además, un QR más grande se lee antes.
             * El margen blanco interior es la «zona tranquila» que el lector
             * necesita alrededor del código.
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(colores.qrFondo)
                    .clickable(enabled = pase != null, onClickLabel = "Ampliar el código", onClick = onAmpliar)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (pase != null) {
                    CodigoQr(pase)
                } else {
                    CircularProgressIndicator(color = colores.marca)
                }
            }

            Spacer(Modifier.height(22.dp))

            CuentaAtras(pase, texto = colores.onMarca, acento = colores.marcaAcento, aviso = colores.avisoSobreMarca)
        }
    }
}

/** El QR del pase. Cuando el código cambia, el nuevo entra con un fundido en vez de saltar de golpe. */
@Composable
private fun CodigoQr(pase: Pase, modifier: Modifier = Modifier) {
    val colores = GymitTema.colores
    Crossfade(targetState = pase.contenido, animationSpec = tween(400), modifier = modifier, label = "qr") { contenido ->
        val qr = remember(contenido) { generarQr(contenido, colores.qrModulo, colores.qrFondo) }
        Image(
            bitmap = qr,
            contentDescription = "Código QR de acceso",
            filterQuality = FilterQuality.None,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * El QR a pantalla completa, sobre blanco, con la foto y el nombre del socio:
 * es la vista que ve el personal del gimnasio, y la foto es lo que le permite
 * comprobar que quien enseña el pase es su titular. Se cierra tocando en
 * cualquier sitio o con «atrás».
 */
@Composable
private fun QrAmpliado(socio: Socio?, selfie: Selfie?, pase: Pase?, onCerrar: () -> Unit) {
    val colores = GymitTema.colores
    val tinta = colores.qrModulo
    IconosOscurosEnBarraDeEstado()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colores.qrFondo)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Cerrar",
                onClick = onCerrar
            )
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onCerrar) {
                Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = tinta)
            }
        }
        Spacer(Modifier.weight(1f))
        if (pase != null) {
            CodigoQr(pase, Modifier.fillMaxWidth().aspectRatio(1f))
        }
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarSocio(selfie = selfie, nombre = socio?.nombre ?: "", tamano = 56.dp, borde = colores.marca)
            Spacer(Modifier.width(14.dp))
            Text(
                socio?.nombre ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = tinta
            )
        }
        Spacer(Modifier.height(24.dp))
        CuentaAtras(
            pase,
            texto = tinta,
            acento = colores.marca,
            aviso = colores.avisoSobreQr,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(Modifier.weight(1f))
    }
}

/**
 * La barra que se vacía en 30 s y los segundos que le quedan al código. En los
 * últimos [SEGUNDOS_AVISO] pasa a ámbar: así nadie enseña en el torno un
 * código que va a cambiar mientras el lector lo está leyendo.
 */
@Composable
private fun CuentaAtras(
    pase: Pase?,
    texto: Color,
    acento: Color,
    aviso: Color,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val restante = pase?.segundosRestantes ?: VIGENCIA_PASE_SEGUNDOS
    val color by animateColorAsState(if (restante <= SEGUNDOS_AVISO) aviso else acento, label = "colorCuentaAtras")
    val fraccion by animateFloatAsState(
        targetValue = restante / VIGENCIA_PASE_SEGUNDOS.toFloat(),
        animationSpec = if (restante == VIGENCIA_PASE_SEGUNDOS) snap() else tween(1000, easing = LinearEasing),
        label = "cuentaAtras"
    )
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Código nuevo en",
                style = MaterialTheme.typography.bodyMedium,
                color = texto.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f)
            )
            Text(
                "$restante s",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(texto.copy(alpha = 0.18f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraccion)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color)
            )
        }
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
            Icon(
                Icons.Outlined.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
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

private const val SEGUNDOS_AVISO = 5

/**
 * Mientras se ve el QR ampliado, que siempre va sobre blanco, los iconos de la
 * barra de estado pasan a oscuros para que se sigan viendo; al cerrarlo
 * vuelven a como estaban.
 */
@Composable
private fun IconosOscurosEnBarraDeEstado() {
    val actividad = LocalContext.current.buscarActividad() ?: return
    DisposableEffect(actividad) {
        val control = WindowCompat.getInsetsController(actividad.window, actividad.window.decorView)
        val anterior = control.isAppearanceLightStatusBars
        control.isAppearanceLightStatusBars = true
        onDispose { control.isAppearanceLightStatusBars = anterior }
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

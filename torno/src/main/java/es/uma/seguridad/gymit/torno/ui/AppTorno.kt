package es.uma.seguridad.gymit.torno.ui

import android.Manifest
import android.app.Activity
import android.view.HapticFeedbackConstants
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.uma.seguridad.gymit.torno.Lectura
import es.uma.seguridad.gymit.torno.Pantalla
import es.uma.seguridad.gymit.torno.VistaModeloTorno
import es.uma.seguridad.gymit.torno.datos.Acceso
import es.uma.seguridad.gymit.torno.datos.Resultado
import es.uma.seguridad.gymit.torno.datos.Veredicto
import es.uma.seguridad.gymit.ui.LogoGymit
import es.uma.seguridad.gymit.ui.PantallaAccesoMarca
import es.uma.seguridad.gymit.ui.marcoEscaneo
import es.uma.seguridad.gymit.ui.theme.GymitTema
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/* ==========================================================================
   INTERFAZ DEL TORNO

   Tres pantallas: el acceso del personal, el escáner (con el veredicto a
   pantalla completa encima) y el registro de accesos. Son pocas y sin enlaces
   profundos, así que basta con un estado en el modelo de vista en lugar de una
   biblioteca de navegación.
   ========================================================================== */

@Composable
fun AppTorno(vistaModelo: VistaModeloTorno) {
    val pantalla by vistaModelo.pantalla.collectAsState()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (pantalla) {
            Pantalla.Acceso -> {
                val estado by vistaModelo.acceso.collectAsState()
                PantallaAccesoPersonal(estado.cargando, estado.error, vistaModelo::entrar)
            }
            Pantalla.Escaner -> {
                val lectura by vistaModelo.lectura.collectAsState()
                val aviso by vistaModelo.aviso.collectAsState()
                PantallaEscaner(
                    personal = vistaModelo.personal,
                    lectura = lectura,
                    aviso = aviso,
                    onLeido = vistaModelo::leido,
                    onCerrarLectura = vistaModelo::cerrarLectura,
                    onCerrarAviso = vistaModelo::cerrarAviso,
                    onRegistro = vistaModelo::abrirRegistro,
                    onSalir = vistaModelo::salir
                )
            }
            Pantalla.Registro -> {
                val registro by vistaModelo.registro.collectAsState()
                BackHandler(onBack = vistaModelo::volverAlEscaner)
                PantallaRegistro(registro, vistaModelo::cargarRegistro, vistaModelo::volverAlEscaner)
            }
        }
    }
}

/* ----------------------------- Acceso del personal ----------------------------- */

/**
 * El mismo diseño de acceso que la app del socio (PantallaAccesoMarca, en
 * :comun), con el fondo oscuro del torno en lugar del índigo de la marca.
 */
@Composable
private fun PantallaAccesoPersonal(cargando: Boolean, error: String?, onEntrar: (String, String) -> Unit) {
    PantallaAccesoMarca(
        titular = "Abre el torno\ndel gimnasio.",
        entradilla = "Acceso para el personal. Entra con tu cuenta de recepción para empezar a leer pases.",
        textoBoton = "Abrir el torno",
        cargando = cargando,
        error = error,
        onEntrar = onEntrar,
        fondoCabecera = MaterialTheme.colorScheme.background,
        textoCabecera = MaterialTheme.colorScheme.onBackground,
        fondoFormulario = GymitTema.colores.tarjeta
    )
}

/* ----------------------------- Escáner ----------------------------- */

@Composable
private fun PantallaEscaner(
    personal: String,
    lectura: Lectura?,
    aviso: String?,
    onLeido: (String) -> Unit,
    onCerrarLectura: () -> Unit,
    onCerrarAviso: () -> Unit,
    onRegistro: () -> Unit,
    onSalir: () -> Unit
) {
    val contexto = LocalContext.current
    var camara by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { camara = it }
    // El torno no tiene otra función que leer pases: el permiso se pide nada más abrir el escáner
    LaunchedEffect(Unit) { if (!camara) lanzador.launch(Manifest.permission.CAMERA) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (camara) {
            VistaCamara(onLeido, Modifier.fillMaxSize())
        } else {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.NoPhotography, null, tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    "El torno necesita la cámara para leer el pase de los socios.",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { lanzador.launch(Manifest.permission.CAMERA) }) { Text("Permitir la cámara") }
            }
        }

        // Cabecera sobre la imagen de la cámara
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f))
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoGymit(tamano = 36.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Torno · Entrada principal", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(personal, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRegistro) {
                Icon(Icons.AutoMirrored.Outlined.ListAlt, "Registro de accesos", tint = Color.White)
            }
            IconButton(onClick = onSalir) {
                Icon(Icons.AutoMirrored.Outlined.Logout, "Cerrar el torno", tint = Color.White)
            }
        }

        // Visor en el centro
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.72f)
                .aspectRatio(1f)
                .marcoEscaneo(GymitTema.colores.marcaAcento, grosor = 6.dp, largo = 44.dp, radio = 20.dp)
        )

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f))
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.QrCodeScanner, null, tint = GymitTema.colores.marcaAcento)
            Spacer(Modifier.height(8.dp))
            Text(
                "Acerca tu pase de Gymit al lector",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (aviso != null) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp).padding(bottom = 90.dp),
                action = { TextButton(onClick = onCerrarAviso) { Text("Vale") } }
            ) { Text(aviso) }
        }

        AnimatedVisibility(
            visible = lectura != null,
            enter = fadeIn() + scaleIn(initialScale = 0.96f),
            exit = fadeOut()
        ) {
            // Se recuerda la última lectura para que la animación de salida no se quede en blanco
            var ultima by remember { mutableStateOf(lectura) }
            if (lectura != null) ultima = lectura
            ultima?.let { PantallaVeredicto(it, onCerrarLectura) }
        }
    }
}

/**
 * Vista previa de la cámara trasera con el analizador de QR.
 *
 * Igual que en el selfie de la app del socio, CameraX se ata al ciclo de vida
 * de la pantalla y suelta la cámara solo al salir.
 */
@Composable
private fun VistaCamara(onLeido: (String) -> Unit, modifier: Modifier) {
    val contexto = LocalContext.current
    val propietario = LocalLifecycleOwner.current
    val vista = remember {
        PreviewView(contexto).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val hilo = remember { Executors.newSingleThreadExecutor() }
    val alLeer by rememberUpdatedState(onLeido)

    DisposableEffect(propietario) {
        val futuro = ProcessCameraProvider.getInstance(contexto)
        var proveedor: ProcessCameraProvider? = null
        futuro.addListener({
            proveedor = futuro.get().also {
                val previa = Preview.Builder().build().apply { setSurfaceProvider(vista.surfaceProvider) }
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply { setAnalyzer(hilo, LectorQr { texto -> alLeer(texto) }) }
                it.unbindAll()
                it.bindToLifecycle(propietario, CameraSelector.DEFAULT_BACK_CAMERA, previa, analisis)
            }
        }, ContextCompat.getMainExecutor(contexto))
        onDispose {
            proveedor?.unbindAll()
            hilo.shutdown()
        }
    }
    AndroidView(factory = { vista }, modifier = modifier)
}

/* ----------------------------- Veredicto ----------------------------- */

/** Pantalla completa en el verde o el rojo del torno, los mismos de la paleta (docs/PALETA.md). */
@Composable
private fun PantallaVeredicto(lectura: Lectura, onCerrar: () -> Unit) {
    val colores = GymitTema.colores
    val concedido = lectura.veredicto is Veredicto.Concedido
    val fondo = if (concedido) colores.acceso else colores.denegado
    val texto = if (concedido) colores.onAcceso else colores.onDenegado
    val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lectura.hora))
    val vista = LocalView.current

    /*
     * Al aparecer cada veredicto: una vibración corta distinta para «adelante»
     * y para «denegado» (el personal la nota sin mirar la pantalla), y los
     * iconos de la barra de estado oscuros sobre el menta y claros sobre el
     * rojo, para que se sigan viendo.
     */
    DisposableEffect(lectura) {
        vista.performHapticFeedback(if (concedido) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
        val ventana = (vista.context as? Activity)?.window
        val control = ventana?.let { WindowCompat.getInsetsController(it, vista) }
        val anterior = control?.isAppearanceLightStatusBars
        control?.isAppearanceLightStatusBars = concedido
        onDispose { if (anterior != null) control.isAppearanceLightStatusBars = anterior }
    }
    // Barra que se vacía en el tiempo que el veredicto se queda en pantalla
    val restante = remember(lectura) { Animatable(1f) }
    LaunchedEffect(lectura) {
        restante.animateTo(0f, tween(VistaModeloTorno.VERDICTO_EN_PANTALLA_MS.toInt(), easing = LinearEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .clickable(onClick = onCerrar)
            .systemBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (concedido) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
            contentDescription = null,
            tint = texto,
            modifier = Modifier.size(140.dp)
        )
        Spacer(Modifier.height(24.dp))
        when (val v = lectura.veredicto) {
            is Veredicto.Concedido -> {
                Text("Adelante", color = texto, style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(8.dp))
                Text(v.nombre, color = texto, style = MaterialTheme.typography.headlineMedium)
                UsuarioVeredicto(v.usuario, texto)
            }
            is Veredicto.Denegado -> {
                Text("Acceso denegado", color = texto, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(v.motivo.texto, color = texto, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                if (v.usuario != null) {
                    UsuarioVeredicto(v.usuario, texto)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(hora, color = texto.copy(alpha = 0.75f), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .fillMaxWidth(0.5f)
                .height(4.dp)
                .clip(CircleShape)
                .background(texto.copy(alpha = 0.2f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(restante.value)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(texto)
            )
        }
    }
}

/** El usuario del socio con el icono de persona, debajo del veredicto. */
@Composable
private fun UsuarioVeredicto(usuario: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Person, contentDescription = "Usuario", tint = color.copy(alpha = 0.75f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(usuario, color = color.copy(alpha = 0.75f), style = MaterialTheme.typography.titleMedium)
    }
}

/* ----------------------------- Registro ----------------------------- */

@Composable
private fun PantallaRegistro(registro: Resultado<List<Acceso>>?, onRecargar: () -> Unit, onVolver: () -> Unit) {
    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver al escáner") }
            Text("Registro de accesos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onRecargar) { Icon(Icons.Outlined.Refresh, "Recargar") }
        }
        when (registro) {
            null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Resultado.Error -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(registro.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is Resultado.Exito -> if (registro.datos.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Todavía no ha pasado nadie por este torno.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(registro.datos, key = { it.id }) { FilaAcceso(it) }
                }
            }
        }
    }
}

@Composable
private fun FilaAcceso(acceso: Acceso) {
    val colores = GymitTema.colores
    val concedido = acceso.titulo.startsWith("Acceso concedido")
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colores.tarjeta),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (concedido) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
                null,
                tint = if (concedido) colores.accesoTexto else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(acceso.titulo.substringAfter(" · "), style = MaterialTheme.typography.titleSmall)
                Text(
                    "${acceso.contenido} · ${acceso.fecha}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                if (concedido) "Concedido" else "Denegado",
                style = MaterialTheme.typography.labelMedium,
                color = if (concedido) colores.accesoTexto else MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .padding(horizontal = 6.dp)
            )
        }
    }
}

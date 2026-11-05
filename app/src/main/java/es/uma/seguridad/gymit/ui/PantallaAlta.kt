package es.uma.seguridad.gymit.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import es.uma.seguridad.gymit.ui.theme.GymitTema
import java.io.ByteArrayOutputStream

/* ==========================================================================
   ALTA CON SELFIE

   El socio se hace una foto con la cámara frontal, en directo. Sirve para que
   en el torno se pueda comprobar que quien entra es el titular del pase.

   Dos decisiones de diseño que importan:
   - No se acepta una foto de la galería: sólo la cámara en directo. Una foto
     de la galería puede ser de cualquiera.
   - El permiso CAMERA se pide en el momento en que hace falta (al pulsar
     «Hacerme el selfie») y con la explicación delante. Si se deniega, no se
     insiste ni se bloquea nada: se puede seguir sin selfie y el resto de la
     aplicación funciona (P2.3, paso 6).
   ========================================================================== */

/** Justificación del permiso ante el usuario, sin tecnicismos (P2.3, paso 6). */
const val JUSTIFICACION_CAMARA =
    "Gymit necesita la cámara para hacerte un selfie al darte de alta, " +
        "y así el gimnasio puede comprobar que quien entra eres tú."

private enum class Paso { Explicacion, Camara, Revision }

/**
 * @param esAlta true la primera vez, justo después de entrar; false cuando se
 *   repite desde el perfil. Sólo cambia el texto del botón para salir.
 */
@Composable
fun PantallaAlta(
    nombre: String,
    esAlta: Boolean,
    onSelfie: (ByteArray) -> Unit,
    onTerminar: () -> Unit
) {
    val contexto = LocalContext.current
    val ambito = rememberCoroutineScope()

    fun tienePermiso() =
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    var paso by remember { mutableStateOf(Paso.Explicacion) }
    var foto by remember { mutableStateOf<Bitmap?>(null) }
    var denegado by remember { mutableStateOf(false) }
    var permanente by remember { mutableStateOf(false) }
    var errorCamara by remember { mutableStateOf(false) }

    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            denegado = false
            permanente = false
            paso = Paso.Camara
        } else {
            denegado = true
            /*
             * Tras dos negativas Android deja de mostrar el diálogo y
             * shouldShowRequestPermissionRationale pasa a false. Se mira AQUÍ,
             * al recibir la respuesta: si se calculase al dibujar, la segunda
             * negativa no cambiaría ningún estado (denegado ya era true), la
             * pantalla no se redibujaría y el botón seguiría ofreciendo un
             * diálogo que ya no va a salir.
             */
            permanente = contexto.buscarActividad()
                ?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == false
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (paso) {
            Paso.Explicacion -> Explicacion(
                nombre = nombre,
                esAlta = esAlta,
                denegado = denegado,
                permanente = permanente,
                errorCamara = errorCamara,
                onContinuar = {
                    errorCamara = false
                    if (tienePermiso()) paso = Paso.Camara else lanzador.launch(Manifest.permission.CAMERA)
                },
                onAjustes = {
                    contexto.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null))
                    )
                },
                onSaltar = onTerminar
            )

            Paso.Camara -> Camara(
                onFoto = {
                    foto = it
                    paso = Paso.Revision
                },
                onError = {
                    errorCamara = true
                    paso = Paso.Explicacion
                },
                onCerrar = { paso = Paso.Explicacion }
            )

            Paso.Revision -> Revision(
                foto = foto,
                onRepetir = {
                    foto = null
                    paso = Paso.Camara
                },
                onUsar = {
                    val elegida = foto ?: return@Revision
                    ambito.launch {
                        onSelfie(withContext(Dispatchers.Default) { aJpeg(elegida) })
                        onTerminar()
                    }
                }
            )
        }
    }
}

@Composable
private fun Explicacion(
    nombre: String,
    esAlta: Boolean,
    denegado: Boolean,
    permanente: Boolean,
    errorCamara: Boolean,
    onContinuar: () -> Unit,
    onAjustes: () -> Unit,
    onSaltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .marcoEscaneo(MaterialTheme.colorScheme.primary, grosor = 5.dp, largo = 26.dp)
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text(
            if (esAlta) "Un último paso, ${nombre.substringBefore(' ')}" else "Actualiza tu selfie",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(10.dp))
        Text(
            JUSTIFICACION_CAMARA,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        Punto(Icons.Outlined.CameraAlt, "Foto en directo con la cámara frontal. No se aceptan fotos de la galería.")
        Punto(Icons.Outlined.VerifiedUser, "Sólo se usa para comprobar en el torno que el pase es tuyo.")

        if (denegado || errorCamara) {
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.NoPhotography, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        when {
                            errorCamara -> "No se ha podido abrir la cámara de este dispositivo."
                            permanente -> "Has denegado la cámara. Para hacerte el selfie, actívala en los ajustes de Gymit."
                            else -> "Sin la cámara no puedes completar el alta con foto. El resto de la app sigue funcionando."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = if (permanente) onAjustes else onContinuar,
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (permanente) "Abrir ajustes" else "Hacerme el selfie")
        }
        TextButton(onClick = onSaltar, modifier = Modifier.fillMaxWidth()) {
            Text(if (esAlta) "Ahora no" else "Cancelar")
        }
    }
}

@Composable
private fun Punto(icono: ImageVector, texto: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Vista previa de la cámara frontal con CameraX y botón para disparar.
 *
 * CameraX se ata al ciclo de vida de la pantalla (bindToLifecycle): la cámara
 * se abre al mostrarse y se libera sola al salir, sin código de limpieza que
 * se pueda olvidar.
 */
@Composable
private fun Camara(onFoto: (Bitmap) -> Unit, onError: () -> Unit, onCerrar: () -> Unit) {
    val contexto = LocalContext.current
    val propietario = LocalLifecycleOwner.current
    val captura = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }
    val vista = remember {
        PreviewView(contexto).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    var disparando by remember { mutableStateOf(false) }

    DisposableEffect(propietario) {
        val futuro = ProcessCameraProvider.getInstance(contexto)
        var proveedor: ProcessCameraProvider? = null
        futuro.addListener({
            try {
                proveedor = futuro.get().also {
                    val previa = Preview.Builder().build().apply { setSurfaceProvider(vista.surfaceProvider) }
                    it.unbindAll()
                    it.bindToLifecycle(propietario, CameraSelector.DEFAULT_FRONT_CAMERA, previa, captura)
                }
            } catch (e: Exception) {
                onError()
            }
        }, ContextCompat.getMainExecutor(contexto))
        onDispose { proveedor?.unbindAll() }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cerrar cámara", tint = Color.White)
                }
            }
            Text(
                "Centra tu cara en el círculo",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
                    .marcoEscaneo(GymitTema.colores.marcaAcento, grosor = 6.dp, largo = 40.dp, radio = 18.dp)
                    .padding(14.dp)
                    .clip(CircleShape)
            ) {
                AndroidView(factory = { vista }, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.weight(1f))
            // Disparador
            Box(
                Modifier
                    .padding(bottom = 32.dp)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = {
                        if (disparando) return@FilledIconButton
                        disparando = true
                        captura.takePicture(
                            ContextCompat.getMainExecutor(contexto),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(imagen: ImageProxy) {
                                    val girada = enderezar(imagen)
                                    disparando = false
                                    onFoto(girada)
                                }

                                override fun onError(excepcion: ImageCaptureException) {
                                    disparando = false
                                    onError()
                                }
                            }
                        )
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (disparando) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    else Icon(Icons.Outlined.CameraAlt, contentDescription = "Hacer la foto")
                }
            }
        }
    }
}

@Composable
private fun Revision(foto: Bitmap?, onRepetir: () -> Unit, onUsar: () -> Unit) {
    var guardando by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("¿Te gusta cómo has salido?", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Asegúrate de que se te ve bien la cara: es lo que verán en el torno.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        if (foto != null) {
            Image(
                bitmap = foto.asImageBitmap(),
                contentDescription = "Tu selfie",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                guardando = true
                onUsar()
            },
            enabled = !guardando,
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Usar esta foto")
        }
        TextButton(onClick = onRepetir, enabled = !guardando, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Repetir")
        }
    }
}

/**
 * Gira la foto según la orientación del sensor y la refleja en horizontal,
 * para que salga igual que se veía en la vista previa (como un espejo).
 * Después la recorta a un cuadrado centrado de 720 px como mucho.
 */
private fun enderezar(imagen: ImageProxy): Bitmap {
    val original = imagen.toBitmap()
    val giro = imagen.imageInfo.rotationDegrees
    imagen.close()
    val matriz = Matrix().apply {
        postRotate(giro.toFloat())
        postScale(-1f, 1f)
    }
    val girada = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
    val lado = minOf(girada.width, girada.height)
    val cuadrada = Bitmap.createBitmap(girada, (girada.width - lado) / 2, (girada.height - lado) / 2, lado, lado)
    return if (lado > 720) Bitmap.createScaledBitmap(cuadrada, 720, 720, true) else cuadrada
}

private fun aJpeg(foto: Bitmap): ByteArray =
    ByteArrayOutputStream().use {
        foto.compress(Bitmap.CompressFormat.JPEG, 88, it)
        it.toByteArray()
    }

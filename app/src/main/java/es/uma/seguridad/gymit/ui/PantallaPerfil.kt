package es.uma.seguridad.gymit.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import es.uma.seguridad.gymit.modelo.*

/* ==========================================================================
   PERFIL DEL SOCIO

   Sus datos (GET /api/perfil), su selfie, el estado del permiso de cámara y
   el cierre de sesión.
   ========================================================================== */

@Composable
fun PantallaPerfil(
    socio: Socio?,
    selfie: Selfie?,
    estado: EstadoUi<Perfil>,
    onCargar: () -> Unit,
    onRehacerSelfie: () -> Unit,
    onSalir: () -> Unit
) {
    val contexto = LocalContext.current
    var camaraConcedida by remember { mutableStateOf(false) }
    var confirmarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onCargar() }
    // El permiso se puede cambiar desde los ajustes del sistema: se vuelve a mirar al volver a la app
    LifecycleResumeEffect(Unit) {
        camaraConcedida = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        onPauseOrDispose { }
    }

    val perfil = (estado as? EstadoUi.Exito)?.datos
    val nombre = perfil?.nombre?.ifBlank { null } ?: socio?.nombre ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CabeceraPestana(titulo = "Perfil")

        // Tarjeta de identidad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AvatarSocio(selfie = selfie, nombre = nombre, tamano = 112.dp)
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onRehacerSelfie),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = "Cambiar selfie",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(nombre, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            val rol = perfil?.rol?.ifBlank { null } ?: socio?.rol
            if (!rol.isNullOrBlank()) {
                Text(
                    if (rol == "administrador") "Administrador" else "Socio",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Seccion("Datos personales") {
            when (estado) {
                is EstadoUi.Reposo, is EstadoUi.Cargando -> Box(
                    Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(Modifier.size(28.dp)) }

                is EstadoUi.Error -> Column(Modifier.padding(16.dp)) {
                    Text(mensajeDeError(estado.motivo), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onCargar) { Text("Reintentar") }
                }

                is EstadoUi.Exito -> {
                    Fila(Icons.Outlined.AlternateEmail, "Usuario", estado.datos.usuario)
                    Separador()
                    Fila(Icons.Outlined.Mail, "Correo", estado.datos.correo)
                    Separador()
                    Fila(Icons.Outlined.Badge, "DNI", estado.datos.dni)
                }
            }
        }

        Seccion("Privacidad y permisos") {
            Fila(
                icono = Icons.Outlined.CameraAlt,
                etiqueta = "Cámara",
                valor = if (camaraConcedida) "Permitida · para el selfie del alta" else "No permitida",
                onClick = onRehacerSelfie
            )
            Separador()
            Fila(
                icono = Icons.Outlined.PhotoCamera,
                etiqueta = "Selfie del alta",
                valor = if (selfie != null) "Hecho · toca para repetirlo" else "Pendiente · toca para hacerlo",
                onClick = onRehacerSelfie
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { confirmarSalida = true },
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }

        Text(
            "Gymit 1.0 · Grupo K · Seguridad en Entornos Móviles",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Se borrarán de este móvil tu historial guardado y tu selfie. Tendrás que volver a entrar para usar tu pase.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onSalir()
                }) { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Text(
        titulo,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 8.dp, bottom = 8.dp)
    )
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        Column(Modifier.padding(vertical = 6.dp), content = contenido)
    }
}

@Composable
private fun Fila(icono: ImageVector, etiqueta: String, valor: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.bodyLarge)
        }
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Separador() = HorizontalDivider(
    modifier = Modifier.padding(start = 66.dp, end = 16.dp),
    color = MaterialTheme.colorScheme.outlineVariant
)

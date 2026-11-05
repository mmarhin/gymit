package es.uma.seguridad.gymit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.uma.seguridad.gymit.ui.theme.GymitTema
import es.uma.seguridad.gymit.modelo.*

/* ==========================================================================
   PERFIL DEL SOCIO

   Sus datos (GET /api/perfil), su foto (el botón de cámara sobre ella repite
   el selfie del alta) y el cierre de sesión.
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
    var confirmarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onCargar() }

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
            // Toda la foto es el botón (112 dp, muy por encima de los 48 dp mínimos);
            // la cámara de la esquina sólo indica que se puede cambiar
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClickLabel = "Cambiar la foto", onClick = onRehacerSelfie)
            ) {
                AvatarSocio(selfie = selfie, nombre = nombre, tamano = 112.dp)
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(nombre, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            val rol = perfil?.rol?.ifBlank { null } ?: socio?.rol
            if (!rol.isNullOrBlank()) {
                Text(
                    if (rol == "administrador") "Personal del gimnasio" else "Socio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Seccion("Datos personales", nota = "Para cambiar tus datos, pásate por recepción.") {
            when (estado) {
                is EstadoUi.Reposo, is EstadoUi.Cargando -> repeat(3) { indice ->
                    if (indice > 0) Separador()
                    FilaEsqueleto()
                }

                is EstadoUi.Error -> Column(Modifier.padding(16.dp)) {
                    Text(mensajeDeError(estado.motivo), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onCargar) { Text("Reintentar") }
                }

                is EstadoUi.Exito -> {
                    Fila(Icons.Outlined.Person, "Usuario", estado.datos.usuario)
                    Separador()
                    Fila(Icons.Outlined.Mail, "Correo", estado.datos.correo)
                    Separador()
                    Fila(Icons.Outlined.Badge, "DNI", estado.datos.dni)
                }
            }
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

        Spacer(Modifier.height(24.dp))
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
private fun Seccion(titulo: String, nota: String? = null, contenido: @Composable ColumnScope.() -> Unit) {
    Text(
        titulo,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 8.dp, bottom = 8.dp)
    )
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GymitTema.colores.tarjeta),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = if (nota != null) 8.dp else 16.dp)
    ) {
        Column(Modifier.padding(vertical = 6.dp), content = contenido)
    }
    if (nota != null) {
        Text(
            nota,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 28.dp, end = 28.dp, bottom = 16.dp)
        )
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
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
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

/** Silueta de una fila mientras llegan los datos del servidor. */
@Composable
private fun FilaEsqueleto() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(24.dp).esqueleto(CircleShape))
        Spacer(Modifier.width(16.dp))
        Column {
            Box(Modifier.width(60.dp).height(10.dp).esqueleto())
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(160.dp).height(14.dp).esqueleto())
        }
    }
}

@Composable
private fun Separador() = HorizontalDivider(
    modifier = Modifier.padding(start = 56.dp, end = 16.dp),
    color = MaterialTheme.colorScheme.outlineVariant
)

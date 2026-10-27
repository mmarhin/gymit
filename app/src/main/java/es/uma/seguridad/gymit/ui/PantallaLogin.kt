package es.uma.seguridad.gymit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import es.uma.seguridad.gymit.modelo.*

/**
 * Pantalla de acceso. ESTÁ TERMINADA: no hace falta tocarla en P2.2.
 *
 * Sigue el patrón de elevación de estado de la sesión 2.5: el composable no
 * guarda el resultado de la operación, lo recibe, y comunica lo que ocurre
 * mediante la función onEntrar.
 */
@Composable
fun PantallaLogin(
    estado: EstadoUi<RespuestaLogin>,
    onEntrar: (String, String) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Acceso", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onEntrar(usuario.trim(), clave) },
            enabled = usuario.isNotBlank() && clave.isNotBlank() && estado !is EstadoUi.Cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (estado is EstadoUi.Cargando) "Entrando..." else "Entrar")
        }

        Spacer(Modifier.height(16.dp))

        // El when es exhaustivo gracias a la clase sellada del TODO 1b
        when (estado) {
            is EstadoUi.Error -> Text(
                text = textoDeError(estado.motivo),
                color = MaterialTheme.colorScheme.error
            )
            is EstadoUi.Cargando -> CircularProgressIndicator()
            else -> Unit
        }
    }
}

/**
 * Traduce el motivo a un texto para el usuario.
 *
 * Fíjate en que el mensaje NO revela detalles internos: nada de direcciones de
 * servidor, rutas ni trazas. Es la idea de la sesión 2.4.5.
 */
private fun textoDeError(motivo: MotivoError): String = when (motivo) {
    MotivoError.SinConexion -> "No se ha podido conectar. Inténtalo de nuevo."
    MotivoError.CredencialesNoValidas -> "Usuario o contraseña incorrectos."
    MotivoError.SesionCaducada -> "La sesión ha caducado. Vuelve a entrar."
    MotivoError.Desconocido -> "Ha ocurrido un error. Inténtalo de nuevo."
}

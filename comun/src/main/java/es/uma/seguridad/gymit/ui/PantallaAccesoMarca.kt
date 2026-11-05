package es.uma.seguridad.gymit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/* ==========================================================================
   PANTALLA DE ACCESO DE MARCA

   El diseño de inicio de sesión que comparten la app del socio y la del
   torno: una cabecera con el logo y un titular, y debajo una hoja redondeada
   con el formulario. Cada app pone sus textos y sus colores.

   En uso normal no hay nada que desplazar: cuando aparece el teclado, el
   titular se oculta y la cabecera se queda sólo con el logo, de modo que el
   formulario y el botón siguen a la vista. La hoja del formulario admite
   scroll sólo como red de seguridad, para cuando ni así cabe (letra grande
   en accesibilidad, móviles pequeños, el aviso de error, pantalla dividida):
   si el contenido cabe, no se mueve.

   Como el resto de pantallas, sólo guarda lo que se va tecleando: el
   resultado del intento (cargando, error) lo recibe de fuera.
   ========================================================================== */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaAccesoMarca(
    titular: String,
    entradilla: String,
    textoBoton: String,
    cargando: Boolean,
    error: String?,
    onEntrar: (usuario: String, clave: String) -> Unit,
    fondoCabecera: Color,
    textoCabecera: Color,
    fondoFormulario: Color,
    modifier: Modifier = Modifier
) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    val puedeEntrar = usuario.isNotBlank() && clave.isNotBlank() && !cargando
    val entrar = { if (puedeEntrar) onEntrar(usuario.trim(), clave) }
    val tecladoVisible = WindowInsets.isImeVisible

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(fondoCabecera)
    ) {
        // Cabecera de marca
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 28.dp, vertical = if (tecladoVisible) 20.dp else 36.dp)
        ) {
            LogoGymit(tamano = if (tecladoVisible) 52.dp else 72.dp)
            AnimatedVisibility(
                visible = !tecladoVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(24.dp))
                    Text(titular, style = MaterialTheme.typography.displaySmall, color = textoCabecera)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        entradilla,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textoCabecera.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Formulario en una hoja que sube desde abajo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(fondoFormulario)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuario") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = MaterialTheme.shapes.medium,
                // Para que el gestor de contraseñas del móvil sepa qué campo es cuál y los rellene
                modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.Username }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            if (claveVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { entrar() }),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.Password }
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = entrar,
                enabled = puedeEntrar,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Entrando…")
                } else {
                    Text(textoBoton)
                }
            }

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                AvisoError(error)
            }
        }
    }
}

@Composable
private fun AvisoError(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.width(10.dp))
        Text(texto, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

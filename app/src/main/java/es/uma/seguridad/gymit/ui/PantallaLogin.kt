package es.uma.seguridad.gymit.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import es.uma.seguridad.gymit.modelo.*
import es.uma.seguridad.gymit.ui.theme.GymitTema

/**
 * Pantalla de acceso.
 *
 * Sigue el patrón de elevación de estado de la sesión 2.5: el composable no
 * guarda el resultado de la operación, lo recibe, y comunica lo que ocurre
 * mediante la función onEntrar. El diseño es el común de las dos apps
 * (PantallaAccesoMarca, en el módulo :comun), sobre el índigo de la marca.
 */
@Composable
fun PantallaLogin(
    estado: EstadoUi<RespuestaLogin>,
    onEntrar: (String, String) -> Unit
) {
    // Sólo se pinta algo para el error; el resto de estados ya se ven en el botón
    val error = when (estado) {
        is EstadoUi.Error -> mensajeDeError(estado.motivo)
        else -> null
    }

    PantallaAccesoMarca(
        titular = "Tu gimnasio,\nen el bolsillo.",
        entradilla = "Entra con tu cuenta de socio y pasa el torno con tu pase digital.",
        textoBoton = "Entrar",
        cargando = estado is EstadoUi.Cargando,
        error = error,
        onEntrar = onEntrar,
        fondoCabecera = GymitTema.colores.marca,
        textoCabecera = GymitTema.colores.onMarca,
        fondoFormulario = MaterialTheme.colorScheme.background
    )
}

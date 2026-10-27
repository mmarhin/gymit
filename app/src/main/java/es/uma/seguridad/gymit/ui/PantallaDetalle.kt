package es.uma.seguridad.gymit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.uma.seguridad.gymit.modelo.*

/* ==========================================================================
   PANTALLA DE DETALLE
   Teoría: sesión 2.5 (Compose, elevación de estado y navegación).

   Esta pantalla SE ENTREGA HECHA, igual que las demás pantallas del proyecto
   base. El motivo es el mismo que en P2.2: lo que se evalúa en esta asignatura
   no es tu destreza con Compose, sino lo que seas capaz de demostrar sobre la
   aplicación. Construir una pantalla más no te enseñaría nada nuevo sobre
   seguridad; en cambio, lo que esta pantalla NO hace sí va a enseñarte bastante
   dentro de un rato.

   Léela buscando una ausencia concreta: aquí no hay ni una sola comprobación
   de sesión. No se pregunta si hay token, ni si la entrada es tuyo, ni si
   deberías estar viendo esto. Eso no es un descuido ni una simplificación
   didáctica: es como se escriben las pantallas en un proyecto real, y es
   correcto que sea así. En el paso 4 comprobarás quién te detiene de verdad.
   ========================================================================== */

/**
 * Muestra una entrada concreto.
 *
 * Aplica la elevación de estado de la sesión 2.5: el composable NO guarda el
 * entrada ni sabe de dónde sale. Recibe un estado y dos funciones, y se limita
 * a dibujar. Toda la lógica vive en el modelo de vista y en el repositorio.
 *
 * Fíjate en los parámetros: `id` es una referencia, no contenido. Por la ruta
 * de navegación viaja el identificador y nada más, que es la regla 2.5.3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(
    id: Int,
    estado: EstadoUi<Entrada>,
    onCargar: (Int) -> Unit,
    onVolver: () -> Unit
) {

    /*
     * LaunchedEffect lanza la carga UNA vez por cada valor distinto de `id`, y
     * la cancela sola si la pantalla desaparece antes de terminar. Es la
     * concurrencia estructurada de la sesión 2.4.5 aplicada a la interfaz: si
     * escribieras onCargar(id) suelto en el cuerpo del composable, se lanzaría
     * una petición en cada redibujado.
     *
     * Consecuencia que importa para el paso 3: basta con que esta pantalla se
     * abra para que la aplicación pida la entrada al servidor. Nadie tiene que
     * pulsar nada.
     */
    LaunchedEffect(id) { onCargar(id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrada $id") },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Volver") }
                }
            )
        }
    ) { relleno ->
        Box(
            modifier = Modifier.fillMaxSize().padding(relleno),
            contentAlignment = Alignment.Center
        ) {
            /*
             * El when cubre las cuatro variantes de la interfaz sellada y NO
             * lleva else. Si mañana añades un estado nuevo a EstadoUi, el
             * compilador se parará aquí y te obligará a decidir qué se muestra.
             * Compara esto con el when de PantallaLogin.kt, que sí tiene un
             * else: allí un estado nuevo pasaría desapercibido.
             */
            when (estado) {
                is EstadoUi.Reposo -> CircularProgressIndicator()
                is EstadoUi.Cargando -> CircularProgressIndicator()

                is EstadoUi.Error -> Text(
                    text = mensajeDeError(estado.motivo),
                    color = MaterialTheme.colorScheme.error
                )

                is EstadoUi.Exito -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp)
                ) {
                    Text(estado.datos.titulo, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Text(estado.datos.contenido, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Text(estado.datos.fecha, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Traduce el motivo del error a un texto para la persona que usa la aplicación.
 *
 * Igual que en PantallaLogin.kt, el mensaje no revela detalles internos: ni
 * direcciones de servidor, ni rutas, ni trazas de excepciones (sesión 2.4.5).
 *
 * PARA EL CUADERNO: cuando en el paso 4 abras esta pantalla sin haber iniciado
 * sesión, aquí aparecerá uno de estos cuatro mensajes. Anota CUÁL, porque te
 * dice exactamente qué capa de la aplicación te ha detenido, y esa es la
 * pregunta central de la práctica de hoy.
 */
private fun mensajeDeError(motivo: MotivoError): String = when (motivo) {
    MotivoError.SinConexion -> "No se ha podido conectar. Inténtalo de nuevo."
    MotivoError.CredencialesNoValidas -> "Usuario o contraseña incorrectos."
    MotivoError.SesionCaducada -> "La sesión ha caducado. Vuelve a entrar."
    MotivoError.Desconocido -> "Ha ocurrido un error. Inténtalo de nuevo."
}

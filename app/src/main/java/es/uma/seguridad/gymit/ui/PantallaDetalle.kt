package es.uma.seguridad.gymit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
                title = { Text("Entrada #$id") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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

                is EstadoUi.Error -> EstadoVacio(
                    icono = if (estado.motivo == MotivoError.SesionCaducada) Icons.Outlined.Lock else Icons.Outlined.CloudOff,
                    titulo = "No se puede mostrar esta entrada",
                    texto = mensajeDeError(estado.motivo)
                )

                is EstadoUi.Exito -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Acceso concedido",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                estado.datos.titulo,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            FilaDato(Icons.Outlined.CalendarMonth, "Fecha", fechaLarga(estado.datos.fecha))
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            FilaDato(Icons.Outlined.Tag, "Identificador", "#${estado.datos.id}")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Detalles",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Text(estado.datos.contenido, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun FilaDato(icono: ImageVector, etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
        Column {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

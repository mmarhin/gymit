package es.uma.seguridad.gymit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.uma.seguridad.gymit.modelo.*

/* ==========================================================================
   HISTORIAL DE ENTRADAS

   Las entradas del socio al gimnasio, del backend o de la copia local en Room
   si no hay conexión (lo decide el repositorio, no esta pantalla). Cada
   tarjeta abre el detalle pasando sólo el identificador por la ruta: es la
   regla 2.5.3, por la ruta viaja la referencia y nunca el contenido.
   ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorial(
    estado: EstadoUi<List<Entrada>>,
    onRecargar: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    // La primera vez que se abre la pestaña se cargan los datos sin esperar a que el socio lo pida
    LaunchedEffect(Unit) {
        if (estado is EstadoUi.Reposo) onRecargar()
    }

    Column(Modifier.fillMaxSize()) {
        CabeceraPestana(
            titulo = "Historial",
            subtitulo = (estado as? EstadoUi.Exito)?.datos?.size?.let { n ->
                if (n == 1) "1 entrada registrada" else "$n entradas registradas"
            } ?: "Tus entradas al gimnasio",
            acciones = {
                IconButton(onClick = onRecargar) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Recargar")
                }
            }
        )

        PullToRefreshBox(
            isRefreshing = estado is EstadoUi.Cargando,
            onRefresh = onRecargar,
            modifier = Modifier.fillMaxSize()
        ) {
            /*
             * El when cubre las cuatro variantes de la interfaz sellada y NO
             * lleva else: si mañana se añade un estado a EstadoUi, el
             * compilador se para aquí y obliga a decidir qué se muestra.
             */
            when (estado) {
                is EstadoUi.Reposo, is EstadoUi.Cargando -> Box(Modifier.fillMaxSize())

                is EstadoUi.Error -> EstadoVacio(
                    icono = Icons.Outlined.CloudOff,
                    titulo = "No se ha podido cargar el historial",
                    texto = mensajeDeError(estado.motivo),
                    modifier = Modifier.fillMaxSize(),
                    accion = { FilledTonalButton(onClick = onRecargar) { Text("Reintentar") } }
                )

                is EstadoUi.Exito -> if (estado.datos.isEmpty()) {
                    EstadoVacio(
                        icono = Icons.Outlined.EventBusy,
                        titulo = "Todavía no has entrado",
                        texto = "Cuando pases el torno con tu pase, tus entradas aparecerán aquí.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(estado.datos, key = { it.id }) { entrada ->
                            TarjetaEntrada(entrada, onClick = { onAbrir(entrada.id) })
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaEntrada(entrada: Entrada, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CasillaFecha(entrada.fecha)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entrada.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    entrada.contenido,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** El día y el mes de la entrada, en una casilla índigo como las de un calendario. */
@Composable
fun CasillaFecha(fecha: String, modifier: Modifier = Modifier) {
    val (dia, mes) = partesFecha(fecha)
    Column(
        modifier = modifier
            .size(52.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(dia, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(mes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

private val MESES = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

/** "2024-03-11" -> ("11", "MAR"). Si el backend manda otro formato, se enseña tal cual. */
private fun partesFecha(fecha: String): Pair<String, String> {
    val trozos = fecha.take(10).split("-")
    val mes = trozos.getOrNull(1)?.toIntOrNull()?.let { MESES.getOrNull(it - 1) }
    val dia = trozos.getOrNull(2)?.toIntOrNull()?.toString()
    return if (dia != null && mes != null) dia to mes else fecha.take(5) to ""
}

/** Fecha larga para el detalle: "2024-03-11" -> "11 de marzo de 2024". */
fun fechaLarga(fecha: String): String {
    val meses = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val trozos = fecha.take(10).split("-")
    val anio = trozos.getOrNull(0)
    val mes = trozos.getOrNull(1)?.toIntOrNull()?.let { meses.getOrNull(it - 1) }
    val dia = trozos.getOrNull(2)?.toIntOrNull()
    return if (anio != null && mes != null && dia != null) "$dia de $mes de $anio" else fecha
}

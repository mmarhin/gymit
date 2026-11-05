package es.uma.seguridad.gymit.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.uma.seguridad.gymit.ui.theme.GymitTema
import es.uma.seguridad.gymit.modelo.*

/* ==========================================================================
   HISTORIAL DE ENTRADAS

   Las entradas del socio al gimnasio, del backend o de la copia local en Room
   si no hay conexión (lo decide el repositorio, no esta pantalla; aquí sólo
   se avisa). Cada tarjeta abre el detalle pasando sólo el identificador por
   la ruta: es la regla 2.5.3, por la ruta viaja la referencia y nunca el
   contenido.
   ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaHistorial(
    estado: EstadoUi<Historial>,
    onRecargar: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    // La primera vez que se abre la pestaña se cargan los datos sin esperar a que el socio lo pida
    LaunchedEffect(Unit) {
        if (estado is EstadoUi.Reposo) onRecargar()
    }

    // Mientras se recarga se sigue enseñando lo último que se cargó, en vez de vaciar la lista
    var ultimo by remember { mutableStateOf<Historial?>(null) }
    if (estado is EstadoUi.Exito) ultimo = estado.datos
    val visible = (estado as? EstadoUi.Exito)?.datos ?: ultimo.takeIf { estado is EstadoUi.Cargando }

    Column(Modifier.fillMaxSize()) {
        CabeceraPestana(
            titulo = "Historial",
            subtitulo = visible?.entradas?.size?.takeIf { it > 0 }?.let { n ->
                if (n == 1) "1 entrada registrada" else "$n entradas registradas"
            } ?: "Tus entradas al gimnasio"
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
                is EstadoUi.Reposo, is EstadoUi.Cargando ->
                    if (visible != null) ListaEntradas(visible, onAbrir) else ListaEsqueleto()

                is EstadoUi.Error -> Desplazable {
                    EstadoVacio(
                        icono = Icons.Outlined.CloudOff,
                        titulo = "No se ha podido cargar el historial",
                        texto = mensajeDeError(estado.motivo),
                        accion = { FilledTonalButton(onClick = onRecargar) { Text("Reintentar") } }
                    )
                }

                is EstadoUi.Exito -> if (estado.datos.entradas.isEmpty()) {
                    Desplazable {
                        EstadoVacio(
                            icono = Icons.Outlined.EventBusy,
                            titulo = "Todavía no has entrado",
                            texto = "Cuando pases el torno con tu pase, tus entradas aparecerán aquí."
                        )
                    }
                } else {
                    ListaEntradas(estado.datos, onAbrir)
                }
            }
        }
    }
}

/**
 * Centra un mensaje en una lista desplazable de un solo elemento. Hace falta
 * para que «tirar para recargar» funcione también con el historial vacío o con
 * un error: el gesto sólo se activa sobre algo que se puede desplazar.
 */
@Composable
private fun Desplazable(contenido: @Composable () -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { contenido() }
        }
    }
}

/** Las entradas agrupadas por mes, con el mes fijo arriba mientras se desplaza. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListaEntradas(historial: Historial, onAbrir: (Int) -> Unit) {
    // Lo más reciente arriba, venga del servidor (que las manda de la más antigua a la más
    // nueva) o de la copia local (que Room ya ordena al revés)
    val porMes = remember(historial.entradas) {
        historial.entradas.sortedByDescending { it.fecha }.groupBy { it.fecha.take(7) }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        if (historial.desdeCopiaLocal) {
            item(key = "sin-conexion") { AvisoSinConexion(Modifier.padding(top = 8.dp, bottom = 4.dp)) }
        }
        porMes.forEach { (mes, entradas) ->
            stickyHeader(key = "mes-$mes") {
                Text(
                    nombreMes(mes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            items(entradas, key = { it.id }) { entrada ->
                TarjetaEntrada(entrada, onClick = { onAbrir(entrada.id) }, modifier = Modifier.padding(bottom = 10.dp))
            }
        }
    }
}

@Composable
private fun AvisoSinConexion(modifier: Modifier = Modifier) {
    val colores = GymitTema.colores
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colores.avisoContenedor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = colores.onAvisoContenedor)
        Spacer(Modifier.width(12.dp))
        Text(
            "Sin conexión. Estas son tus últimas entradas guardadas.",
            style = MaterialTheme.typography.bodyMedium,
            color = colores.onAvisoContenedor
        )
    }
}

/** Siluetas de tarjetas mientras llega la primera carga. */
@Composable
private fun ListaEsqueleto() {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Box(Modifier.width(120.dp).height(14.dp).esqueleto())
        Spacer(Modifier.height(16.dp))
        repeat(5) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(GymitTema.colores.tarjeta)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(52.dp).esqueleto(MaterialTheme.shapes.medium))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth(0.6f).height(16.dp).esqueleto())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(0.85f).height(12.dp).esqueleto())
                }
            }
        }
    }
}

private val MESES_LARGOS = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

/** "2024-03" -> "Marzo de 2024". */
private fun nombreMes(clave: String): String {
    val anio = clave.take(4)
    val mes = clave.drop(5).toIntOrNull()?.let { MESES_LARGOS.getOrNull(it - 1) }
    return if (mes != null && anio.length == 4) "$mes de $anio" else "Otras fechas"
}

@Composable
private fun TarjetaEntrada(entrada: Entrada, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GymitTema.colores.tarjeta)
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

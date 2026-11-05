package es.uma.seguridad.gymit.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import es.uma.seguridad.gymit.VistaModelo
import es.uma.seguridad.gymit.modelo.EstadoUi

/* ==========================================================================
   NAVEGACIÓN

   Rutas de la aplicación:

     login                Acceso
     alta?primera={bool}  Selfie del alta (primera vez, o repetirlo desde el perfil)
     pase                 \
     historial             > Pestañas, con la barra inferior
     perfil               /
     detalle/{id}         Detalle de una entrada. También se abre desde fuera
                          con el enlace profundo gymit://entrada/{id}

   La navegación decide qué se OFRECE, no qué se PERMITE (sesión 2.5.3). Que
   sin sesión no se vean las pestañas no impide abrir el detalle con el enlace
   profundo: lo que impide ver los datos es el repositorio, que sin token no
   pide nada al servidor (P2.3, paso 4).
   ========================================================================== */

private object Rutas {
    const val LOGIN = "login"
    const val ALTA = "alta?primera={primera}"
    const val PASE = "pase"
    const val HISTORIAL = "historial"
    const val PERFIL = "perfil"
    const val DETALLE = "detalle/{id}"

    fun alta(primera: Boolean) = "alta?primera=$primera"
    fun detalle(id: Int) = "detalle/$id"
}

private data class Pestana(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val PESTANAS = listOf(
    Pestana(Rutas.PASE, "Pase", Icons.Outlined.QrCode2),
    Pestana(Rutas.HISTORIAL, "Historial", Icons.Outlined.History),
    Pestana(Rutas.PERFIL, "Perfil", Icons.Outlined.Person)
)

@Composable
fun Navegacion(vistaModelo: VistaModelo) {
    val navController = rememberNavController()
    val estadoLogin by vistaModelo.estadoLogin.collectAsState()
    val estadoLista by vistaModelo.estadoLista.collectAsState()
    val estadoDetalle by vistaModelo.estadoDetalle.collectAsState()
    val estadoPerfil by vistaModelo.estadoPerfil.collectAsState()
    val socio by vistaModelo.socio.collectAsState()
    val selfie by vistaModelo.selfie.collectAsState()

    // Con una sesión guardada se entra directamente al pase
    val inicio = remember { if (vistaModelo.haySesion()) Rutas.PASE else Rutas.LOGIN }

    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        // Cada pantalla reserva el hueco de las barras del sistema; aquí sólo el de la barra de pestañas
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (PESTANAS.any { it.ruta == rutaActual }) {
                BarraPestanas(rutaActual, navController)
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = inicio,
            modifier = Modifier.padding(relleno),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {

            composable(Rutas.LOGIN) {
                PantallaLogin(
                    estado = estadoLogin,
                    onEntrar = { usuario, clave -> vistaModelo.entrar(usuario, clave) }
                )
                // La navegación es un efecto: va en LaunchedEffect, no suelta en el cuerpo del composable
                LaunchedEffect(estadoLogin) {
                    if (estadoLogin is EstadoUi.Exito) {
                        val destino = if (vistaModelo.tieneSelfie()) Rutas.PASE else Rutas.alta(primera = true)
                        navController.navigate(destino) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                }
            }

            composable(
                route = Rutas.ALTA,
                arguments = listOf(navArgument("primera") { type = NavType.BoolType; defaultValue = false })
            ) { entrada ->
                val primera = entrada.arguments?.getBoolean("primera") ?: false
                PantallaAlta(
                    nombre = socio?.nombre ?: "",
                    esAlta = primera,
                    onSelfie = { vistaModelo.guardarSelfie(it) },
                    onTerminar = {
                        if (primera) {
                            navController.navigate(Rutas.PASE) {
                                popUpTo(Rutas.ALTA) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(Rutas.PASE) {
                val pase by vistaModelo.pase.collectAsState()
                val selfieComprobado by vistaModelo.selfieComprobado.collectAsState()
                PantallaPase(
                    socio = socio,
                    selfie = selfie,
                    pase = pase,
                    selfieComprobado = selfieComprobado,
                    onHacerSelfie = { navController.navigate(Rutas.alta(primera = false)) }
                )
            }

            composable(Rutas.HISTORIAL) {
                PantallaHistorial(
                    estado = estadoLista,
                    onRecargar = { vistaModelo.cargarEntradas() },
                    onAbrir = { identificador -> navController.navigate(Rutas.detalle(identificador)) }
                )
            }

            composable(Rutas.PERFIL) {
                PantallaPerfil(
                    socio = socio,
                    selfie = selfie,
                    estado = estadoPerfil,
                    onCargar = { vistaModelo.cargarPerfil() },
                    onRehacerSelfie = { navController.navigate(Rutas.alta(primera = false)) },
                    onSalir = {
                        vistaModelo.salir()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
                deepLinks = listOf(navDeepLink { uriPattern = "gymit://entrada/{id}" })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                PantallaDetalle(
                    id = id,
                    estado = estadoDetalle,
                    onCargar = { vistaModelo.cargarDetalle(it) },
                    onVolver = {
                        // Si se llegó por el enlace profundo no hay pantalla anterior: se va al inicio
                        if (!navController.popBackStack()) navController.navigate(inicio)
                    }
                )
            }
        }
    }
}

@Composable
private fun BarraPestanas(rutaActual: String?, navController: NavHostController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        PESTANAS.forEach { pestana ->
            NavigationBarItem(
                selected = rutaActual == pestana.ruta,
                onClick = {
                    navController.navigate(pestana.ruta) {
                        // Patrón estándar de pestañas: una sola copia de cada una y se conserva su estado.
                        // El pase es siempre la raíz de las pestañas, se llegue desde el acceso o con sesión guardada.
                        popUpTo(Rutas.PASE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(pestana.icono, contentDescription = null) },
                label = { Text(pestana.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

package es.uma.seguridad.gymit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import es.uma.seguridad.gymit.VistaModelo
import es.uma.seguridad.gymit.modelo.EstadoUi

/**
 * Navegación entre pantallas. ESTÁ TERMINADA: no hace falta tocarla en P2.2.
 *
 * En la práctica P2.3 se ampliará con argumentos de ruta y enlaces profundos,
 * y se comprobará que la navegación no es un control de seguridad (sesión 2.5.3).
 */
@Composable
fun Navegacion(vistaModelo: VistaModelo) {
    val navController = rememberNavController()
    val estadoLogin by vistaModelo.estadoLogin.collectAsState()
    val estadoLista by vistaModelo.estadoLista.collectAsState()
    val estadoDetalle by vistaModelo.estadoDetalle.collectAsState()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            PantallaLogin(
                estado = estadoLogin,
                onEntrar = { usuario, clave -> vistaModelo.entrar(usuario, clave) }
            )
            if (estadoLogin is EstadoUi.Exito) {
                navController.navigate("lista") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("lista") {
            PantallaLista(
                estado = estadoLista,
                onRecargar = { vistaModelo.cargarEntradas() },
                onSalir = {
                    vistaModelo.salir()
                    navController.navigate("login") {
                        popUpTo("lista") { inclusive = true }
                    }
                },
                onAbrir = { identificador -> navController.navigate("detalle/$identificador") }
            )
        }

        composable(
            route = "detalle/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = "gymit://entrada/{id}" })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            PantallaDetalle(
                id = id,
                estado = estadoDetalle,
                onCargar = { vistaModelo.cargarDetalle(it) },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

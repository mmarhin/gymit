package es.uma.seguridad.gymit

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import es.uma.seguridad.gymit.datos.*
import es.uma.seguridad.gymit.ui.Navegacion
import es.uma.seguridad.gymit.ui.theme.GymitTheme

/**
 * Punto de entrada. ESTÁ TERMINADO: no hace falta tocarlo en P2.2.
 *
 * Las dependencias se construyen aquí a mano. En un proyecto real se usaría
 * inyección de dependencias (sesión 2.5.4); lo importante es la idea: el
 * cliente de red y el almacén se configuran UNA VEZ y toda la aplicación los
 * hereda. Cuando en la práctica P3.2 se añada anclaje de certificados, se
 * tocará sólo ClienteRed y no habrá que revisar ninguna pantalla.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val repositorio = Repositorio(
            api = ClienteRed.crear(),
            sesion = SesionLocal(applicationContext),
            dao = BaseDatos.obtener(applicationContext).entradaDao()
        )

        setContent {
            GymitTheme {
                val vistaModelo: VistaModelo = viewModel(factory = FabricaVistaModelo(repositorio))
                Navegacion(vistaModelo)
            }
        }
    }
}

class FabricaVistaModelo(private val repositorio: Repositorio) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        VistaModelo(repositorio) as T
}

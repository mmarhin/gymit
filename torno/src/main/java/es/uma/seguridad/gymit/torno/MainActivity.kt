package es.uma.seguridad.gymit.torno

import android.os.Bundle
import android.view.WindowManager
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import es.uma.seguridad.gymit.torno.datos.RepositorioTorno
import es.uma.seguridad.gymit.torno.ui.AppTorno
import es.uma.seguridad.gymit.ui.theme.GymitTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tema siempre oscuro: iconos claros en las barras del sistema
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        // Un torno está siempre esperando al siguiente socio: la pantalla no se apaga
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val repositorio = RepositorioTorno(applicationContext)
        setContent {
            // Siempre en oscuro: es una pantalla fija en la entrada, y el verde y el rojo destacan más
            GymitTheme(darkTheme = true) {
                AppTorno(viewModel(factory = FabricaVistaModeloTorno(repositorio)))
            }
        }
    }
}

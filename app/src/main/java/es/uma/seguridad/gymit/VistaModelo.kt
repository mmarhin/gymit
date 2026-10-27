package es.uma.seguridad.gymit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uma.seguridad.gymit.datos.Repositorio
import es.uma.seguridad.gymit.modelo.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Modelo de vista de la aplicación.
 *
 * Sostiene el estado de las pantallas y lanza el trabajo en viewModelScope, de
 * modo que las corrutinas se cancelan solas cuando la pantalla desaparece
 * (sesión 2.4.5, concurrencia estructurada). Si el usuario sale mientras una
 * descarga está en marcha, esa descarga se cancela: no termina después y
 * escribe datos de la sesión anterior.
 *
 * Está completo. En P2.2 sólo hay que leerlo.
 */
class VistaModelo(private val repositorio: Repositorio) : ViewModel() {

    private val _estadoLogin = MutableStateFlow<EstadoUi<RespuestaLogin>>(EstadoUi.Reposo)
    val estadoLogin: StateFlow<EstadoUi<RespuestaLogin>> = _estadoLogin.asStateFlow()

    private val _estadoLista = MutableStateFlow<EstadoUi<List<Entrada>>>(EstadoUi.Reposo)
    val estadoLista: StateFlow<EstadoUi<List<Entrada>>> = _estadoLista.asStateFlow()

    private val _estadoDetalle = MutableStateFlow<EstadoUi<Entrada>>(EstadoUi.Reposo)
    val estadoDetalle: StateFlow<EstadoUi<Entrada>> = _estadoDetalle.asStateFlow()

    fun entrar(usuario: String, clave: String) {
        viewModelScope.launch {
            _estadoLogin.value = EstadoUi.Cargando
            _estadoLogin.value = repositorio.iniciarSesion(usuario, clave)
        }
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            _estadoDetalle.value = EstadoUi.Cargando
            _estadoDetalle.value = repositorio.obtenerEntrada(id)
        }
    }

    fun cargarEntradas() {
        viewModelScope.launch {
            _estadoLista.value = EstadoUi.Cargando
            _estadoLista.value = repositorio.obtenerEntradas()
        }
    }

    /**
     * Cierre de sesión.
     *
     * Pide al repositorio que borre la sesión y devuelve las dos pantallas a
     * Reposo. Lo segundo no es decorativo: si el estado de acceso se quedara
     * en Exito, la navegación volvería a entrar sola en la lista.
     *
     * PARA EL CUADERNO: además del estado del modelo de vista y del token,
     * ¿qué otros rastros del usuario quedan en el dispositivo? Haz la lista
     * antes de seguir y compruébala en la sección 5.5.
     */
    fun salir() {
        viewModelScope.launch {
            repositorio.cerrarSesion()
            _estadoLogin.value = EstadoUi.Reposo
            _estadoLista.value = EstadoUi.Reposo
            _estadoDetalle.value = EstadoUi.Reposo
        }
    }
}

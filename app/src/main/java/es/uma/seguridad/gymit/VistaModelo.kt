package es.uma.seguridad.gymit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uma.seguridad.gymit.datos.Repositorio
import es.uma.seguridad.gymit.modelo.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
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
 * Además del estado de cada pantalla, guarda quién es el socio, su selfie y el
 * pase vigente, que comparten varias pantallas.
 */
class VistaModelo(private val repositorio: Repositorio) : ViewModel() {

    private val _estadoLogin = MutableStateFlow<EstadoUi<RespuestaLogin>>(EstadoUi.Reposo)
    val estadoLogin: StateFlow<EstadoUi<RespuestaLogin>> = _estadoLogin.asStateFlow()

    private val _estadoLista = MutableStateFlow<EstadoUi<Historial>>(EstadoUi.Reposo)
    val estadoLista: StateFlow<EstadoUi<Historial>> = _estadoLista.asStateFlow()

    /**
     * El servidor ha rechazado el token guardado (por ejemplo, porque se ha
     * reiniciado y ha olvidado las sesiones). La interfaz lo avisa y ofrece
     * volver a entrar; no se borra nada hasta que el socio lo pida.
     */
    private val _sesionRechazada = MutableStateFlow(false)
    val sesionRechazada: StateFlow<Boolean> = _sesionRechazada.asStateFlow()

    /**
     * Mira si una respuesta es «sesión caducada» teniendo una sesión guardada.
     * Sin sesión guardada (por ejemplo, al abrir el enlace profundo sin haber
     * entrado, paso 4 de la P2.3) no se avisa: la pantalla ya enseña el error.
     */
    private fun <T> vigilar(estado: EstadoUi<T>): EstadoUi<T> {
        if (estado is EstadoUi.Error && estado.motivo == MotivoError.SesionCaducada && repositorio.socio() != null) {
            _sesionRechazada.value = true
        }
        return estado
    }

    fun olvidarAvisoSesion() {
        _sesionRechazada.value = false
    }

    private val _estadoDetalle = MutableStateFlow<EstadoUi<Entrada>>(EstadoUi.Reposo)
    val estadoDetalle: StateFlow<EstadoUi<Entrada>> = _estadoDetalle.asStateFlow()

    private val _estadoPerfil = MutableStateFlow<EstadoUi<Perfil>>(EstadoUi.Reposo)
    val estadoPerfil: StateFlow<EstadoUi<Perfil>> = _estadoPerfil.asStateFlow()

    private val _socio = MutableStateFlow(repositorio.socio())
    val socio: StateFlow<Socio?> = _socio.asStateFlow()

    private val _selfie = MutableStateFlow<Selfie?>(null)
    val selfie: StateFlow<Selfie?> = _selfie.asStateFlow()

    /** Pasa a true cuando ya se ha mirado en disco si hay selfie, para no anunciar el alta antes de saberlo. */
    private val _selfieComprobado = MutableStateFlow(false)
    val selfieComprobado: StateFlow<Boolean> = _selfieComprobado.asStateFlow()

    /**
     * El pase vigente, recalculado al empezar cada segundo para que la cuenta
     * atrás avance y el QR cambie al acabar el periodo.
     *
     * WhileSubscribed hace que el reloj sólo corra mientras alguna pantalla lo
     * está mirando: al salir de la pantalla del pase, se para a los cinco
     * segundos y deja de generar códigos.
     */
    val pase: StateFlow<Pase?> = flow {
        while (true) {
            val ahora = System.currentTimeMillis()
            emit(repositorio.paseActual(ahora))
            delay(1000 - ahora % 1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            _selfie.value = repositorio.obtenerSelfie()
            _selfieComprobado.value = true
        }
    }

    /** ¿Hay sesión guardada? Decide la pantalla con la que arranca la app. */
    fun haySesion(): Boolean = repositorio.socio() != null

    /** ¿Ha completado el socio el alta con selfie? */
    suspend fun tieneSelfie(): Boolean = repositorio.obtenerSelfie() != null

    fun guardarSelfie(jpeg: ByteArray) {
        viewModelScope.launch { _selfie.value = repositorio.guardarSelfie(jpeg) }
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _estadoPerfil.value = EstadoUi.Cargando
            _estadoPerfil.value = vigilar(repositorio.obtenerPerfil())
        }
    }

    fun entrar(usuario: String, clave: String) {
        viewModelScope.launch {
            _estadoLogin.value = EstadoUi.Cargando
            val resultado = repositorio.iniciarSesion(usuario, clave)
            if (resultado is EstadoUi.Exito) {
                _socio.value = repositorio.socio()
                _selfie.value = repositorio.obtenerSelfie()
            }
            _estadoLogin.value = resultado
        }
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            _estadoDetalle.value = EstadoUi.Cargando
            _estadoDetalle.value = vigilar(repositorio.obtenerEntrada(id))
        }
    }

    fun cargarEntradas() {
        viewModelScope.launch {
            _estadoLista.value = EstadoUi.Cargando
            _estadoLista.value = vigilar(repositorio.obtenerEntradas())
        }
    }

    /**
     * Cierre de sesión.
     *
     * Pide al repositorio que borre la sesión y devuelve todas las pantallas a
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
            _estadoPerfil.value = EstadoUi.Reposo
            _socio.value = null
            _selfie.value = null
            _sesionRechazada.value = false
        }
    }
}

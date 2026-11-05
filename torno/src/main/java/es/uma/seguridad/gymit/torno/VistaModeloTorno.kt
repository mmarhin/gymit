package es.uma.seguridad.gymit.torno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import es.uma.seguridad.gymit.torno.datos.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Pantalla { Acceso, Escaner, Registro }

/** Estado del formulario de acceso del personal. */
data class EstadoAcceso(val cargando: Boolean = false, val error: String? = null)

/** El veredicto que se está enseñando a pantalla completa, con la hora a la que se leyó. */
data class Lectura(val veredicto: Veredicto, val hora: Long)

class VistaModeloTorno(private val repositorio: RepositorioTorno) : ViewModel() {

    private val validador = ValidadorPase(socios = { repositorio.socios })

    private val _pantalla = MutableStateFlow(if (repositorio.haySesion()) Pantalla.Escaner else Pantalla.Acceso)
    val pantalla: StateFlow<Pantalla> = _pantalla.asStateFlow()

    private val _acceso = MutableStateFlow(EstadoAcceso())
    val acceso: StateFlow<EstadoAcceso> = _acceso.asStateFlow()

    private val _lectura = MutableStateFlow<Lectura?>(null)
    val lectura: StateFlow<Lectura?> = _lectura.asStateFlow()

    private val _registro = MutableStateFlow<Resultado<List<Acceso>>?>(null)
    val registro: StateFlow<Resultado<List<Acceso>>?> = _registro.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    val personal: String get() = repositorio.nombrePersonal()

    private var ultimoTexto: String? = null
    private var ultimaVez = 0L
    @Volatile private var validando = false
    private var ocultar: Job? = null

    init {
        if (repositorio.haySesion()) viewModelScope.launch { cargarSocios() }
    }

    fun entrar(usuario: String, clave: String) {
        viewModelScope.launch {
            _acceso.value = EstadoAcceso(cargando = true)
            when (val r = repositorio.entrar(usuario, clave)) {
                is Resultado.Exito -> {
                    _acceso.value = EstadoAcceso()
                    _pantalla.value = Pantalla.Escaner
                }
                is Resultado.Error -> _acceso.value = EstadoAcceso(error = r.mensaje)
            }
        }
    }

    /** Pide la lista de socios; si falla, lo avisa y devuelve false. */
    private suspend fun cargarSocios(): Boolean {
        val r = repositorio.cargarSocios()
        if (r is Resultado.Error) {
            _aviso.value = r.mensaje
            if (!repositorio.haySesion()) _pantalla.value = Pantalla.Acceso
            return false
        }
        _aviso.value = null
        return true
    }

    /**
     * Llega desde el hilo de la cámara cada vez que se lee un QR.
     *
     * La cámara lee el mismo código muchas veces por segundo mientras el socio
     * lo tiene delante, así que se ignora el mismo texto durante unos segundos:
     * si no, tras «Adelante» saldría enseguida «Este código ya se ha usado».
     */
    fun leido(texto: String) {
        val ahora = System.currentTimeMillis()
        if (_lectura.value != null) return
        if (texto == ultimoTexto && ahora - ultimaVez < IGNORAR_REPETIDO_MS) return
        if (validando) return
        ultimoTexto = texto
        ultimaVez = ahora
        validando = true

        viewModelScope.launch {
            // Si la lista de socios no se pudo cargar (sin red al arrancar, servidor
            // caído), se vuelve a pedir ahora: si no, todo pase saldría como socio
            // desconocido hasta reiniciar la app
            if (repositorio.socios.isEmpty() && !cargarSocios()) {
                validando = false
                return@launch
            }
            val veredicto = validador.validar(texto)
            _lectura.value = Lectura(veredicto, ahora)
            validando = false
            ocultar?.cancel()
            ocultar = viewModelScope.launch {
                delay(VERDICTO_EN_PANTALLA_MS)
                _lectura.value = null
            }
            if (!repositorio.registrar(veredicto)) {
                ocultar?.cancel()
                _lectura.value = null
                _acceso.value = EstadoAcceso(error = "La sesión del torno ha caducado. Vuelve a entrar.")
                _pantalla.value = Pantalla.Acceso
            }
        }
    }

    fun cerrarLectura() {
        ocultar?.cancel()
        _lectura.value = null
    }

    fun abrirRegistro() {
        _pantalla.value = Pantalla.Registro
        cargarRegistro()
    }

    fun cargarRegistro() {
        viewModelScope.launch {
            _registro.value = null
            _registro.value = repositorio.registro()
        }
    }

    fun volverAlEscaner() {
        _pantalla.value = Pantalla.Escaner
    }

    fun cerrarAviso() {
        _aviso.value = null
    }

    fun salir() {
        repositorio.salir()
        _lectura.value = null
        _registro.value = null
        _pantalla.value = Pantalla.Acceso
    }

    companion object {
        const val VERDICTO_EN_PANTALLA_MS = 3_000L
        const val IGNORAR_REPETIDO_MS = 8_000L
    }
}

class FabricaVistaModeloTorno(private val repositorio: RepositorioTorno) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = VistaModeloTorno(repositorio) as T
}

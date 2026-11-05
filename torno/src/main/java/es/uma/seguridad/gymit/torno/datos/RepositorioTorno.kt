package es.uma.seguridad.gymit.torno.datos

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Lo que puede salir de una operación del torno. */
sealed interface Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>
    data class Error(val mensaje: String) : Resultado<Nothing>
}

/**
 * Única puerta del torno a la red y a la sesión.
 *
 * La sesión del personal se guarda en unas preferencias en claro, igual que la
 * del socio en el Bloque II: para la demostración basta, y se protegerá junto
 * con la otra.
 */
class RepositorioTorno(contexto: Context, private val api: ApiTorno = ClienteRed.crear()) {

    private val preferencias = contexto.getSharedPreferences("sesion_torno", Context.MODE_PRIVATE)

    /** usuario -> nombre de todas las cuentas, cargado al entrar. */
    @Volatile
    var socios: Map<String, String> = emptyMap()
        private set

    private val token get() = preferencias.getString("token", null)

    fun haySesion() = token != null

    fun nombrePersonal(): String = preferencias.getString("nombre", null) ?: "Personal"

    /**
     * Entra con una cuenta de personal. Una cuenta de socio no puede manejar el
     * torno: la comprobación se hace aquí, en el cliente, y por tanto es sólo
     * de cara a la interfaz; el backend común no distingue roles (hallazgo
     * previsto para el Bloque IV).
     */
    suspend fun entrar(usuario: String, clave: String): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val respuesta = api.login(PeticionLogin(usuario, clave))
            if (respuesta.rol != "administrador") {
                return@withContext Resultado.Error("Esta cuenta es de socio. El torno lo maneja el personal del gimnasio.")
            }
            preferencias.edit()
                .putString("token", respuesta.token)
                .putString("nombre", respuesta.nombre)
                .apply()
            cargarSocios()
        } catch (e: HttpException) {
            Resultado.Error("Usuario o contraseña incorrectos.")
        } catch (e: IOException) {
            Resultado.Error("No se ha podido conectar con el servidor.")
        } catch (e: Exception) {
            Resultado.Error("Ha ocurrido un error. Inténtalo de nuevo.")
        }
    }

    suspend fun cargarSocios(): Resultado<Unit> = withContext(Dispatchers.IO) {
        val token = token ?: return@withContext Resultado.Error("No hay sesión.")
        try {
            socios = api.cuentas("Bearer $token").usuarios.associate { it.usuario to it.nombre }
            Resultado.Exito(Unit)
        } catch (e: HttpException) {
            // El backend guarda las sesiones en memoria: si se reinicia, el token deja de valer
            if (e.code() == 401) salir()
            Resultado.Error("La sesión ha caducado. Vuelve a entrar.")
        } catch (e: Exception) {
            Resultado.Error("No se ha podido cargar la lista de socios.")
        }
    }

    /**
     * Apunta un acceso en el registro del torno. Si falla la red, el torno
     * sigue funcionando. Devuelve false si el servidor ha rechazado la sesión
     * (por ejemplo, porque se ha reiniciado): entonces se cierra la sesión del
     * torno para que el personal vuelva a entrar y los accesos no se pierdan.
     */
    suspend fun registrar(veredicto: Veredicto): Boolean = withContext(Dispatchers.IO) {
        val token = token ?: return@withContext false
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val acceso = when (veredicto) {
            is Veredicto.Concedido -> NuevoAcceso("Acceso concedido · ${veredicto.nombre}", "${veredicto.usuario} · $hora")
            is Veredicto.Denegado -> NuevoAcceso(
                "Acceso denegado · ${veredicto.motivo.texto}",
                "${veredicto.usuario ?: "desconocido"} · $hora"
            )
        }
        try {
            api.registrar("Bearer $token", acceso)
            true
        } catch (e: HttpException) {
            if (e.code() == 401) {
                salir()
                false
            } else true
        } catch (e: Exception) {
            true
        }
    }

    suspend fun registro(): Resultado<List<Acceso>> = withContext(Dispatchers.IO) {
        val token = token ?: return@withContext Resultado.Error("No hay sesión.")
        try {
            Resultado.Exito(api.registro("Bearer $token").elementos.reversed())
        } catch (e: Exception) {
            Resultado.Error("No se ha podido cargar el registro.")
        }
    }

    fun salir() {
        preferencias.edit().clear().apply()
        socios = emptyMap()
    }
}

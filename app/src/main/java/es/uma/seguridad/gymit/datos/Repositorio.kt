package es.uma.seguridad.gymit.datos

import android.util.Log
import es.uma.seguridad.gymit.modelo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/* ==========================================================================
   EL REPOSITORIO
   Teoría: sesión 2.5.4 (arquitectura por capas) y 2.4.5 (corrutinas).

   Es la ÚNICA puerta de acceso a los datos. La interfaz no sabe si la
   información viene de la red o de la base de datos local, y no debe saberlo.
   Cuando en el Bloque III se añada cifrado o anclaje de certificados se hará
   aquí y en el cliente de red, nunca en las pantallas.

   Fíjate en que todo el trabajo va dentro de withContext(Dispatchers.IO): es
   la regla de la sesión 2.4.5, nada de red ni de disco en el hilo principal.
   ========================================================================== */

class Repositorio(
    private val api: ApiServicio,
    private val sesion: SesionLocal,
    private val dao: EntradaDao,
    private val selfies: AlmacenSelfie,
    private val generadorPase: GeneradorPase = GeneradorPase()
) {

    /** El socio con la sesión abierta, o null si no hay sesión. */
    fun socio(): Socio? {
        val usuario = sesion.obtenerUsuario() ?: return null
        if (!sesion.haySesion()) return null
        return Socio(
            usuario = usuario,
            nombre = sesion.obtenerNombre() ?: usuario,
            rol = sesion.obtenerRol() ?: ""
        )
    }

    /**
     * El pase vigente del socio, o null si no hay sesión. No toca la red: el
     * QR se genera en el móvil, que es lo que permite enseñarlo en el torno
     * aunque no haya cobertura.
     */
    fun paseActual(ahoraMs: Long = System.currentTimeMillis()): Pase? {
        val usuario = sesion.obtenerUsuario() ?: return null
        return generadorPase.pase(usuario, ahoraMs)
    }

    /** El selfie del socio con la sesión abierta, si ya se lo ha hecho. */
    suspend fun obtenerSelfie(): Selfie? = withContext(Dispatchers.IO) {
        sesion.obtenerUsuario()?.let { selfies.obtener(it) }
    }

    suspend fun guardarSelfie(jpeg: ByteArray): Selfie? = withContext(Dispatchers.IO) {
        sesion.obtenerUsuario()?.let { selfies.guardar(it, jpeg) }
    }

    /**
     * Inicia sesión contra el backend y guarda el token si todo va bien.
     *
     * Devuelve un EstadoUi en lugar de lanzar excepciones: el fallo es un
     * estado más del flujo normal, como se vio en la sesión 2.4.5.
     */
    suspend fun iniciarSesion(usuario: String, clave: String): EstadoUi<RespuestaLogin> =
        withContext(Dispatchers.IO) {
            try {
                val respuesta = api.login(PeticionLogin(usuario, clave))

                /* ----------------------------------------------------------
                   EL REGISTRO DELIBERADO

                   Esta línea es el ejemplo de la sesión 2.4.3 puesto en una
                   aplicación de verdad. RespuestaLogin es una clase de datos,
                   así que su toString() lo genera el compilador e incluye
                   TODOS los campos... incluido el token.

                   Nadie ha escrito «imprime el token»: han escrito «imprime
                   el objeto». El resultado es el mismo.

                   En la sección 5.1 lo verás en logcat. Después prueba a
                   comentar esta línea, vuelve a ejecutar y comprueba que el
                   token desaparece: así demuestras la causa en lugar de
                   suponerla.
                   ---------------------------------------------------------- */
                Log.d("Repositorio", "Login correcto: $respuesta")

                sesion.guardarSesion(respuesta.token, respuesta.usuario, respuesta.nombre, respuesta.rol)
                EstadoUi.Exito(respuesta)

            } catch (e: HttpException) {
                /*
                 * El servidor ha respondido, pero con un código de error.
                 * 401 es contraseña incorrecta y 404 usuario inexistente: los
                 * tratamos IGUAL a propósito, para no confirmar qué cuentas
                 * existen. Es la idea de la sesión 2.4.5 sobre los mensajes
                 * que cuentan de más.
                 */
                when (e.code()) {
                    401, 404 -> EstadoUi.Error(MotivoError.CredencialesNoValidas)
                    else -> EstadoUi.Error(MotivoError.Desconocido)
                }
            } catch (e: IOException) {
                // No ha habido respuesta: red caída, backend apagado, dirección mal puesta
                EstadoUi.Error(MotivoError.SinConexion)
            } catch (e: Exception) {
                /*
                 * Cualquier otra cosa. Fíjate en lo que NO se hace: no se
                 * devuelve e.message a la interfaz. Esos mensajes llevan rutas,
                 * consultas y direcciones de servidor.
                 */
                EstadoUi.Error(MotivoError.Desconocido)
            }
        }

    /**
     * Descarga las entradas del usuario, guarda una copia local y las devuelve.
     * Si la red o el servidor fallan, devuelve la copia local en lugar de un
     * error, marcada para que la pantalla avise de que puede no estar al día.
     *
     * Si el servidor rechaza el token (401, por ejemplo porque se ha
     * reiniciado y ha olvidado las sesiones), devuelve SesionCaducada. No
     * borra nada: la copia local sigue en el disco hasta que el socio cierre
     * sesión, igual que antes.
     */
    suspend fun obtenerEntradas(): EstadoUi<Historial> =
        withContext(Dispatchers.IO) {

            /*
             * El operador ?: de la sesión 2.4.2 en su uso más típico: si no hay
             * token se sale de la función con un estado de error. No hay !!, no
             * hay comprobación que se pueda olvidar, y el compilador no deja
             * continuar con un valor que podría faltar.
             */
            val token = sesion.obtenerToken()
                ?: return@withContext EstadoUi.Error(MotivoError.SesionCaducada)
            val propietario = sesion.obtenerUsuario() ?: ""

            try {
                val respuesta = api.entradas("Bearer $token")

                // Copia local, marcada con su propietario
                dao.guardar(
                    respuesta.entradas.map { entrada ->
                        EntradaEntidad(
                            id = entrada.id,
                            titulo = entrada.titulo,
                            contenido = entrada.contenido,
                            fecha = entrada.fecha,
                            propietario = propietario
                        )
                    }
                )
                EstadoUi.Exito(Historial(respuesta.entradas))

            } catch (e: HttpException) {
                // Un fallo del servidor no es falta de conexión: sin copia local se dice tal cual
                if (e.code() == 401) EstadoUi.Error(MotivoError.SesionCaducada)
                else copiaLocal(propietario, MotivoError.Desconocido)
            } catch (e: IOException) {
                copiaLocal(propietario, MotivoError.SinConexion)
            } catch (e: Exception) {
                copiaLocal(propietario, MotivoError.Desconocido)
            }
        }

    private suspend fun copiaLocal(propietario: String, motivo: MotivoError): EstadoUi<Historial> {
        /*
         * Si la red falla recurrimos a la copia local. Éste es el motivo por
         * el que existe la base de datos, y también el motivo por el que los
         * datos del usuario están en el disco del dispositivo, sin cifrar,
         * hasta que alguien los borre.
         */
        val copiaLocal = dao.obtenerDe(propietario)
        return if (copiaLocal.isEmpty()) {
            EstadoUi.Error(motivo)
        } else {
            EstadoUi.Exito(
                Historial(
                    entradas = copiaLocal.map { fila -> Entrada(fila.id, fila.titulo, fila.contenido, fila.fecha) },
                    desdeCopiaLocal = true
                )
            )
        }
    }

    suspend fun obtenerPerfil(): EstadoUi<Perfil> = withContext(Dispatchers.IO) {
        val token = sesion.obtenerToken()
            ?: return@withContext EstadoUi.Error(MotivoError.SesionCaducada)
        try {
            EstadoUi.Exito(api.perfil("Bearer $token"))
        } catch (e: HttpException) {
            EstadoUi.Error(if (e.code() == 401) MotivoError.SesionCaducada else MotivoError.Desconocido)
        } catch (e: IOException) {
            EstadoUi.Error(MotivoError.SinConexion)
        } catch (e: Exception) {
            EstadoUi.Error(MotivoError.Desconocido)
        }
    }

    suspend fun obtenerEntrada(id: Int): EstadoUi<Entrada> =
        withContext(Dispatchers.IO) {
            val token = sesion.obtenerToken()
                ?: return@withContext EstadoUi.Error(MotivoError.SesionCaducada)
            try {
                EstadoUi.Exito(api.entrada("Bearer $token", id))
            } catch (e: HttpException) {
                EstadoUi.Error(if (e.code() == 401) MotivoError.SesionCaducada else MotivoError.Desconocido)
            } catch (e: IOException) {
                EstadoUi.Error(MotivoError.SinConexion)
            } catch (e: Exception) {
                EstadoUi.Error(MotivoError.Desconocido)
            }
        }

    /**
     * Cierra la sesión.
     *
     * Borra el token y el usuario de las preferencias, que es lo que hace que
     * la aplicación deje de estar autenticada.
     *
     * PARA EL CUADERNO: ¿basta con esto para que no quede nada del usuario en
     * el dispositivo? Escribe tu respuesta ANTES de seguir y compruébala en la
     * sección 5.5 de la práctica. Fíjate en qué funciones ofrece el DAO y en
     * cuáles se están llamando de verdad.
     */
    suspend fun cerrarSesion() {
        withContext(Dispatchers.IO) {
            // P2.2 · 5.6: primero se averigua de quién son las filas locales y se
            // borran; la sesión se vacía la ÚLTIMA. Al revés, obtenerUsuario()
            // devolvería null y no se borraría nada. El selfie es un sitio más
            // donde quedan datos del usuario, así que también se borra.
            val propietario = sesion.obtenerUsuario()
            if (propietario != null) {
                dao.borrarDe(propietario)
                selfies.borrar(propietario)
            }
            sesion.cerrarSesion()
        }
    }
}

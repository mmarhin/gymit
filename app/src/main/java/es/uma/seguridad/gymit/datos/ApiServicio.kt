package es.uma.seguridad.gymit.datos

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import es.uma.seguridad.gymit.modelo.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*

/* ==========================================================================
   EL ACCESO AL BACKEND
   Teoría: sesión 2.5.5 (red) y 2.4.5 (corrutinas).

   AVISO: el backend sirve por HTTP SIN CIFRAR. Es deliberado: en la práctica
   P3.2 interceptarás este tráfico con un proxy y después aplicarás TLS y
   anclaje de certificados para ver la diferencia con tus propios ojos.
   ========================================================================== */

/**
 * La interfaz describe la API: una función por endpoint. Retrofit genera en
 * tiempo de ejecución la clase que hace las peticiones de verdad, así que aquí
 * no hay ni una línea de código de red.
 *
 * Todas son `suspend`, de modo que sólo pueden llamarse desde una corrutina y
 * nunca bloquean el hilo de la interfaz.
 */
interface ApiServicio {

    /** El cuerpo se serializa a JSON a partir de la clase de datos. */
    @POST("api/login")
    suspend fun login(@Body peticion: PeticionLogin): RespuestaLogin

    /**
     * La cabecera de autorización viaja como parámetro: quien llame tiene que
     * componer el valor "Bearer <token>". Lo hace el Repositorio.
     */
    @GET("api/elementos")
    suspend fun entradas(@Header("Authorization") autorizacion: String): RespuestaEntradas

    @GET("api/elementos/{id}")
    suspend fun entrada(
        @Header("Authorization") autorizacion: String,
        @Path("id") id: Int
    ): Entrada

    @GET("api/perfil")
    suspend fun perfil(@Header("Authorization") autorizacion: String): Perfil
}

object ClienteRed {

    /**
     * Dirección del backend.
     *
     *   Emulador de Android            http://10.0.2.2:8080/
     *   Dispositivo físico en tu red   http://<IP_de_tu_ordenador>:8080/
     *
     * El emulador tiene su propia interfaz de red: dentro de él, localhost es
     * el propio emulador y no tu ordenador. De ahí el 10.0.2.2.
     *
     * PARA EL CUADERNO: esta dirección queda escrita en el artefacto y se
     * recupera decompilándolo, tal como comprobaste en la práctica P2.1.
     * ¿Es eso un problema? ¿Y si en lugar de una dirección fuera una clave?
     */
    const val BASE_URL = "http://localhost:8080/"

    fun crear(): ApiServicio {

        /*
         * El interceptor de registro imprime en logcat cada petición y cada
         * respuesta. Con nivel BODY imprime también el cuerpo, así que en la
         * petición de acceso saldrán el usuario y la contraseña en claro.
         *
         * Es comodísimo mientras se desarrolla y es un desastre si llega a la
         * versión publicada.
         *
         * PARA EL CUADERNO: ¿qué ocurriría si este interceptor se publicase?
         * ¿Quién podría leer esos registros, y con qué privilegios? Se retoma
         * en la sesión 3.6.
         */
        val registro = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        /*
         * El cliente HTTP se construye UNA VEZ y lo hereda toda la aplicación.
         * Es la idea de la sesión 2.5.4: cuando en P3.2 se añada el anclaje de
         * certificados se tocará sólo aquí, y ninguna pantalla se entera.
         */
        val cliente = OkHttpClient.Builder()
            .addInterceptor(registro)
            .build()

        /*
         * ignoreUnknownKeys = true evita que la aplicación se caiga si el
         * servidor añade un campo que nosotros no conocemos. Es tolerancia
         * razonable, y no debe confundirse con validar la respuesta, que es
         * otra cosa y se trata en la sesión 2.5.5.
         */
        val json = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiServicio::class.java)
    }
}

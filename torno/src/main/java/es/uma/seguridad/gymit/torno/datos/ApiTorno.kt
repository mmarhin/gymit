package es.uma.seguridad.gymit.torno.datos

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.*

/* ==========================================================================
   EL TORNO CONTRA EL BACKEND COMÚN

   El torno es un dispositivo del gimnasio: entra con una cuenta de personal y
   usa el mismo backend que la app del socio, sin modificarlo (P2.3: «el
   backend no cambia»). De él necesita tres cosas:

   - POST /api/login            entrar con la cuenta de personal
   - GET  /api/admin/usuarios   saber qué socios existen y cómo se llaman
   - POST/GET /api/elementos    apuntar cada acceso y consultar el registro

   Limitación del backend común: POST /api/elementos guarda el elemento en la
   lista del dueño del token. El torno no tiene el token del socio, así que el
   acceso queda en el registro del TORNO, no en el historial del socio. El
   historial de cada socio con sus entradas reales llega con Supabase en el
   Bloque III, cuando sea el servidor quien valide el pase y apunte la entrada.

   Igual que la app del socio, habla por HTTP sin cifrar (P3.2).
   ========================================================================== */

@Serializable
data class PeticionLogin(val usuario: String, val clave: String)

@Serializable
data class RespuestaLogin(val token: String, val usuario: String, val nombre: String, val rol: String)

@Serializable
data class Cuenta(val usuario: String, val nombre: String, val rol: String = "")

@Serializable
data class RespuestaCuentas(val usuarios: List<Cuenta>)

@Serializable
data class NuevoAcceso(val titulo: String, val contenido: String)

/** Un acceso del registro del torno. En el backend común es un «elemento». */
@Serializable
data class Acceso(val id: Int = 0, val titulo: String = "", val contenido: String = "", val fecha: String = "")

@Serializable
data class RespuestaAccesos(val elementos: List<Acceso>)

interface ApiTorno {

    @POST("api/login")
    suspend fun login(@Body peticion: PeticionLogin): RespuestaLogin

    @GET("api/admin/usuarios")
    suspend fun cuentas(@Header("Authorization") autorizacion: String): RespuestaCuentas

    @POST("api/elementos")
    suspend fun registrar(@Header("Authorization") autorizacion: String, @Body acceso: NuevoAcceso): Acceso

    @GET("api/elementos")
    suspend fun registro(@Header("Authorization") autorizacion: String): RespuestaAccesos
}

object ClienteRed {

    /** La misma dirección que la app del socio: el puerto 8080 del ordenador llega por adb reverse. */
    const val BASE_URL = "http://localhost:8080/"

    fun crear(): ApiTorno = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient())
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ApiTorno::class.java)
}

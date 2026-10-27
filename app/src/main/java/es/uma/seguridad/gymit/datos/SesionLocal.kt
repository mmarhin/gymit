package es.uma.seguridad.gymit.datos

import android.content.Context
import android.content.SharedPreferences

/* ==========================================================================
   LA SESIÓN DEL USUARIO
   Teoría: sesión 2.2.5 (almacenamiento) y 2.5.5 (persistencia).

   Guarda el token de sesión y el nombre de usuario para que la aplicación
   recuerde quién ha entrado entre un arranque y el siguiente.

   AVISO: aquí se guarda SIN NINGUNA PROTECCIÓN, a propósito. En la sección 5
   de la práctica vas a leer este mismo fichero desde adb y verás el token en
   texto claro. En la práctica P3.1 lo sustituirás por una clave del almacén
   del sistema (Android Keystore).
   ========================================================================== */

class SesionLocal(contexto: Context) {

    /**
     * SharedPreferences es un fichero XML dentro del directorio privado de la
     * aplicación. MODE_PRIVATE significa que ninguna otra aplicación puede
     * leerlo... siempre que nadie tenga acceso al dispositivo con adb y la
     * aplicación sea depurable. Ésa es exactamente la suposición que vamos a
     * romper en la sección 5.
     */
    private val preferencias: SharedPreferences =
        contexto.getSharedPreferences(NOMBRE_FICHERO, Context.MODE_PRIVATE)

    /**
     * Guarda la sesión: dos cadenas, en claro, en un XML.
     *
     * apply() escribe en memoria y vuelca a disco en segundo plano. La
     * alternativa, commit(), escribe de forma síncrona y devuelve si ha ido
     * bien; aquí no hace falta y bloquearía el hilo que llama.
     */
    fun guardarSesion(token: String, usuario: String) {
        preferencias.edit()
            .putString(CLAVE_TOKEN, token)
            .putString(CLAVE_USUARIO, usuario)
            .apply()
    }

    /**
     * Devuelve el token, o null si no hay sesión.
     *
     * Fíjate en el tipo: String?, con interrogación. Es la garantía de la
     * sesión 2.4.2. Quien llame a esta función está obligado por el compilador
     * a decidir qué pasa cuando no hay token, y no puede olvidarse.
     */
    fun obtenerToken(): String? = preferencias.getString(CLAVE_TOKEN, null)

    /** El usuario de la sesión, o null. Sirve para saber de quién son los datos locales. */
    fun obtenerUsuario(): String? = preferencias.getString(CLAVE_USUARIO, null)

    /**
     * Borra los datos de sesión de ESTE fichero.
     *
     * clear() vacía el XML entero, así que después la aplicación ya no está
     * autenticada. Otra cosa es que no quede nada del usuario en el
     * dispositivo: eso lo comprobarás tú en la sección 5.5.
     */
    fun cerrarSesion() {
        preferencias.edit().clear().apply()
    }

    fun haySesion(): Boolean = obtenerToken() != null

    companion object {
        const val NOMBRE_FICHERO = "sesion"
        const val CLAVE_TOKEN = "token"
        const val CLAVE_USUARIO = "usuario"
    }
}

package es.uma.seguridad.gymit.datos

import android.content.Context
import es.uma.seguridad.gymit.modelo.Selfie
import java.io.File

/* ==========================================================================
   EL SELFIE DEL ALTA

   Guarda la foto que el socio se hace con la cámara frontal al darse de alta.
   El torno la mostrará para comprobar que quien entra es el titular.

   Va en el directorio privado de la aplicación (files/selfie/), nunca en la
   galería, y una por usuario.

   AVISO: el fichero se escribe SIN CIFRAR, como el resto de datos locales del
   Bloque II. Con adb y la aplicación depurable se saca con
       adb exec-out run-as es.uma.seguridad.gymit cat files/selfie/<usuario>.jpg
   En la práctica P3.1 se cifrará con una clave del Android Keystore.
   ========================================================================== */

class AlmacenSelfie(contexto: Context) {

    private val carpeta = File(contexto.filesDir, CARPETA)

    private fun fichero(usuario: String) = File(carpeta, "$usuario.jpg")

    fun guardar(usuario: String, jpeg: ByteArray): Selfie {
        carpeta.mkdirs()
        val destino = fichero(usuario)
        destino.writeBytes(jpeg)
        return Selfie(destino.absolutePath, destino.lastModified())
    }

    fun obtener(usuario: String): Selfie? =
        fichero(usuario).takeIf { it.exists() }?.let { Selfie(it.absolutePath, it.lastModified()) }

    fun borrar(usuario: String) {
        fichero(usuario).delete()
    }

    companion object {
        const val CARPETA = "selfie"
    }
}

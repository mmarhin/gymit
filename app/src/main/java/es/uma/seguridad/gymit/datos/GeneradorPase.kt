package es.uma.seguridad.gymit.datos

import es.uma.seguridad.gymit.modelo.Pase
import es.uma.seguridad.gymit.modelo.VIGENCIA_PASE_SEGUNDOS
import es.uma.seguridad.gymit.pase.ContenidoPase
import es.uma.seguridad.gymit.pase.FormatoPase
import java.security.SecureRandom

/* ==========================================================================
   EL PASE DE ACCESO

   Genera el texto que va dentro del QR que el socio enseña en el torno. El
   formato está en el módulo :comun (pase/FormatoPase.kt), porque lo comparte
   con la app del torno, que es quien lo lee:

       gymit:v1:<usuario>:<emitido>:<nonce>

   - emitido: el segundo (hora Unix) en que empezó el periodo actual. El torno
     rechazará los pases con más de 30 segundos.
   - nonce: 16 bytes aleatorios, nuevos en cada periodo, para que el torno
     pueda rechazar un pase que ya se ha usado.

   AVISO: el pase va SIN FIRMAR. Cualquiera que conozca el formato puede
   fabricar uno válido para otro socio con sólo cambiar el usuario. Es el
   punto de partida del Bloque III: allí se añadirá una firma hecha con una
   clave del Android Keystore, ligada a la huella, y el servidor la verificará.
   ========================================================================== */

class GeneradorPase(private val aleatorio: SecureRandom = SecureRandom()) {

    private var periodoActual = -1L
    private var usuarioActual = ""
    private var nonceActual = ""

    /**
     * El pase vigente en el instante [ahoraMs]. Dentro del mismo periodo de 30
     * segundos devuelve siempre el mismo contenido, así que el QR no cambia en
     * cada redibujado; sólo baja la cuenta atrás.
     */
    @Synchronized
    fun pase(usuario: String, ahoraMs: Long): Pase {
        val segundos = ahoraMs / 1000
        val periodo = segundos / VIGENCIA_PASE_SEGUNDOS
        // Nonce nuevo en cada periodo y también si cambia el socio: si no, quien
        // entra tras otro socio en el mismo periodo heredaría su nonce y el torno
        // tomaría su pase por uno ya usado
        if (periodo != periodoActual || usuario != usuarioActual) {
            periodoActual = periodo
            usuarioActual = usuario
            nonceActual = nuevoNonce()
        }
        val emitido = periodo * VIGENCIA_PASE_SEGUNDOS
        return Pase(
            contenido = FormatoPase.componer(ContenidoPase(usuario, emitido, nonceActual)),
            emitido = emitido,
            segundosRestantes = (emitido + VIGENCIA_PASE_SEGUNDOS - segundos).toInt()
        )
    }

    private fun nuevoNonce(): String {
        val bytes = ByteArray(16)
        aleatorio.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

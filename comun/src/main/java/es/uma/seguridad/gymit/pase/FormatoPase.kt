package es.uma.seguridad.gymit.pase

/* ==========================================================================
   FORMATO DEL PASE QR

   El texto que va dentro del QR, compartido por quien lo genera (la app del
   socio) y quien lo lee (la app del torno):

       gymit:v1:<usuario>:<emitido>:<nonce>

   - usuario: el nombre de usuario del socio en el backend.
   - emitido: el segundo (hora Unix) en que empezó el periodo de 30 s.
   - nonce:   16 bytes aleatorios en hexadecimal, nuevos en cada periodo.

   AVISO: el pase va SIN FIRMAR, a propósito (Bloque II). Cualquiera que
   conozca este formato puede fabricar un pase válido para otro socio con sólo
   cambiar el usuario, y el torno no tiene forma de notarlo. En el Bloque III
   se añadirá un sexto campo con la firma, hecha con una clave del Android
   Keystore del móvil del socio, y se verificará antes de abrir el torno.
   ========================================================================== */

/** Lo que contiene un pase una vez leído. */
data class ContenidoPase(
    val usuario: String,
    val emitido: Long,
    val nonce: String
)

object FormatoPase {

    const val PREFIJO = "gymit"
    const val VERSION = "v1"

    /** Segundos que vale cada código antes de que el móvil genere otro. */
    const val VIGENCIA_SEGUNDOS = 30

    private val USUARIO = Regex("^[a-z0-9._-]{1,32}$")
    private val NONCE = Regex("^[0-9a-f]{32}$")

    /**
     * El texto del QR para [contenido]. Exige las mismas reglas que [leer]: un
     * pase que el torno no sabría leer es un error de programación, y es
     * mejor que salte aquí que como «código no válido» delante del torno.
     */
    fun componer(contenido: ContenidoPase): String {
        require(USUARIO.matches(contenido.usuario)) { "Usuario no válido para un pase: ${contenido.usuario}" }
        require(NONCE.matches(contenido.nonce)) { "Nonce no válido para un pase" }
        require(contenido.emitido > 0) { "Hora de emisión no válida para un pase" }
        return "$PREFIJO:$VERSION:${contenido.usuario}:${contenido.emitido}:${contenido.nonce}"
    }

    /**
     * Lee el texto de un QR. Devuelve null si no es un pase de Gymit bien
     * formado: un QR cualquiera, una versión desconocida o un campo con
     * caracteres que no tocan.
     *
     * Ojo: que el formato sea correcto NO significa que el pase sea auténtico.
     * Eso sólo se podrá saber cuando lleve firma (Bloque III).
     */
    fun leer(texto: String): ContenidoPase? {
        val campos = texto.trim().split(":")
        if (campos.size != 5) return null
        val (prefijo, version, usuario, emitido, nonce) = campos
        if (prefijo != PREFIJO || version != VERSION) return null
        if (!USUARIO.matches(usuario) || !NONCE.matches(nonce)) return null
        val segundos = emitido.toLongOrNull()?.takeIf { it > 0 } ?: return null
        return ContenidoPase(usuario, segundos, nonce)
    }
}

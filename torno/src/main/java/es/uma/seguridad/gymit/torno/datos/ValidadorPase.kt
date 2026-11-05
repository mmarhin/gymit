package es.uma.seguridad.gymit.torno.datos

import es.uma.seguridad.gymit.pase.FormatoPase

/* ==========================================================================
   VALIDACIÓN DEL PASE EN EL TORNO

   Decide si el texto leído de un QR deja pasar o no, con tres comprobaciones:

   1. Formato: tiene que ser un pase de Gymit (FormatoPase.leer).
   2. Caducidad: el código no puede tener más de 30 segundos, con un pequeño
      margen para el desfase de reloj entre el móvil y el torno. Tampoco puede
      venir del futuro.
   3. Uso único: un nonce que ya ha pasado no vuelve a pasar. Es lo que impide
      que dos personas entren con el mismo código en sus 30 segundos de vida.

   Y una cuarta que depende del backend: que el socio exista.

   LO QUE NO COMPRUEBA, a propósito (Bloque II): que el pase sea auténtico. Sin
   firma, un pase fabricado a mano con el usuario de otro socio, la hora actual
   y un nonce inventado pasa las tres comprobaciones. En el Bloque III se
   verificará la firma hecha con la clave del Keystore del móvil del socio, y
   la validación se moverá al servidor (Edge Function de Supabase), que es
   donde debe estar: un torno no debería ser quien decide.
   ========================================================================== */

sealed interface Veredicto {
    data class Concedido(val usuario: String, val nombre: String) : Veredicto
    data class Denegado(val motivo: Motivo, val usuario: String? = null) : Veredicto
}

enum class Motivo(val texto: String) {
    NoEsUnPase("Este código no es un pase de Gymit"),
    Caducado("El código ha caducado. Enseña el código nuevo"),
    HoraNoValida("La hora del código no es válida"),
    YaUsado("Este código ya se ha usado"),
    SocioDesconocido("Socio no encontrado")
}

/**
 * @param socios usuario -> nombre de los socios dados de alta.
 * @param reloj hora actual en milisegundos; se inyecta para poder probarlo.
 */
class ValidadorPase(
    private val socios: () -> Map<String, String>,
    private val reloj: () -> Long = System::currentTimeMillis
) {

    /** Nonces ya usados y el segundo en que se emitieron, para poder olvidarlos cuando caducan. */
    private val usados = LinkedHashMap<String, Long>()

    @Synchronized
    fun validar(texto: String): Veredicto {
        val pase = FormatoPase.leer(texto) ?: return Veredicto.Denegado(Motivo.NoEsUnPase)
        val ahora = reloj() / 1000
        olvidarCaducados(ahora)

        val edad = ahora - pase.emitido
        if (edad < -MARGEN_RELOJ) return Veredicto.Denegado(Motivo.HoraNoValida, pase.usuario)
        if (edad > FormatoPase.VIGENCIA_SEGUNDOS + MARGEN_RELOJ) return Veredicto.Denegado(Motivo.Caducado, pase.usuario)
        if (pase.nonce in usados) return Veredicto.Denegado(Motivo.YaUsado, pase.usuario)

        val nombre = socios()[pase.usuario] ?: return Veredicto.Denegado(Motivo.SocioDesconocido, pase.usuario)

        // Se marca como usado sólo si deja pasar: un intento fallido no gasta el código
        usados[pase.nonce] = pase.emitido
        return Veredicto.Concedido(pase.usuario, nombre)
    }

    /** Un nonce caducado ya no hace falta recordarlo: la comprobación de caducidad lo rechaza antes. */
    private fun olvidarCaducados(ahora: Long) {
        val limite = ahora - FormatoPase.VIGENCIA_SEGUNDOS - MARGEN_RELOJ
        usados.entries.removeAll { it.value < limite }
    }

    companion object {
        /** Segundos de tolerancia por el desfase de reloj entre el móvil y el torno. */
        const val MARGEN_RELOJ = 5
    }
}

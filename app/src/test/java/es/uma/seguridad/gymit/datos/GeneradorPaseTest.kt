package es.uma.seguridad.gymit.datos

import es.uma.seguridad.gymit.pase.FormatoPase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GeneradorPaseTest {

    private val generador = GeneradorPase()
    private val ahora = 1_793_883_630_000L

    private fun nonce(usuario: String, ms: Long) =
        FormatoPase.leer(generador.pase(usuario, ms).contenido)!!.nonce

    @Test
    fun dentroDelPeriodoElPaseNoCambia() {
        assertEquals(generador.pase("ana", ahora).contenido, generador.pase("ana", ahora + 5_000).contenido)
    }

    @Test
    fun cadaPeriodoTraeOtroNonce() {
        assertNotEquals(nonce("ana", ahora), nonce("ana", ahora + 30_000))
    }

    @Test
    fun otroSocioEnElMismoPeriodoTraeOtroNonce() {
        assertNotEquals(nonce("ana", ahora), nonce("luis", ahora))
    }
}

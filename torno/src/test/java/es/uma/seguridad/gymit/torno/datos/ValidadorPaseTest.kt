package es.uma.seguridad.gymit.torno.datos

import es.uma.seguridad.gymit.pase.ContenidoPase
import es.uma.seguridad.gymit.pase.FormatoPase
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidadorPaseTest {

    private var ahora = 1_793_883_630_000L
    private val validador = ValidadorPase(socios = { mapOf("ana" to "Ana Ruiz") }, reloj = { ahora })

    private fun pase(usuario: String = "ana", emitido: Long = ahora / 1000, nonce: String = "a".repeat(32)) =
        FormatoPase.componer(ContenidoPase(usuario, emitido, nonce))

    @Test
    fun dejaPasarUnPaseVigente() {
        assertEquals(Veredicto.Concedido("ana", "Ana Ruiz"), validador.validar(pase()))
    }

    @Test
    fun rechazaLoQueNoEsUnPase() {
        assertEquals(Veredicto.Denegado(Motivo.NoEsUnPase), validador.validar("https://example.com"))
    }

    @Test
    fun rechazaUnPaseCaducado() {
        val viejo = pase(emitido = ahora / 1000 - 40)
        assertEquals(Veredicto.Denegado(Motivo.Caducado, "ana"), validador.validar(viejo))
    }

    @Test
    fun aceptaElDesfaseDeRelojDentroDelMargen() {
        assertEquals(Veredicto.Concedido("ana", "Ana Ruiz"), validador.validar(pase(emitido = ahora / 1000 - 33)))
        assertEquals(Veredicto.Concedido("ana", "Ana Ruiz"), validador.validar(pase(emitido = ahora / 1000 + 3, nonce = "b".repeat(32))))
    }

    @Test
    fun rechazaUnPaseDelFuturo() {
        assertEquals(Veredicto.Denegado(Motivo.HoraNoValida, "ana"), validador.validar(pase(emitido = ahora / 1000 + 60)))
    }

    @Test
    fun unCodigoSoloSirveUnaVez() {
        val codigo = pase()
        assertEquals(Veredicto.Concedido("ana", "Ana Ruiz"), validador.validar(codigo))
        assertEquals(Veredicto.Denegado(Motivo.YaUsado, "ana"), validador.validar(codigo))
    }

    @Test
    fun unIntentoFallidoNoGastaElCodigo() {
        val desconocido = ValidadorPase(socios = { emptyMap() }, reloj = { ahora })
        assertEquals(Veredicto.Denegado(Motivo.SocioDesconocido, "ana"), desconocido.validar(pase()))
        assertEquals(Veredicto.Denegado(Motivo.SocioDesconocido, "ana"), desconocido.validar(pase()))
    }

    @Test
    fun rechazaASociosQueNoExisten() {
        assertEquals(Veredicto.Denegado(Motivo.SocioDesconocido, "pepe"), validador.validar(pase(usuario = "pepe")))
    }

    /** Documenta la debilidad del Bloque II: sin firma, un pase fabricado a mano pasa. */
    @Test
    fun sinFirmaUnPaseFabricadoPasa() {
        val fabricado = "gymit:v1:ana:${ahora / 1000}:${"f".repeat(32)}"
        assertEquals(Veredicto.Concedido("ana", "Ana Ruiz"), validador.validar(fabricado))
    }
}

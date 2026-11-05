package es.uma.seguridad.gymit.pase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatoPaseTest {

    private val nonce = "0123456789abcdef0123456789abcdef"

    @Test
    fun componerYLeerSonInversas() {
        val pase = ContenidoPase("ana", 1793883630, nonce)
        assertEquals(pase, FormatoPase.leer(FormatoPase.componer(pase)))
    }

    @Test
    fun leeElFormatoEsperado() {
        assertEquals(
            ContenidoPase("ana", 1793883630, nonce),
            FormatoPase.leer("gymit:v1:ana:1793883630:$nonce")
        )
    }

    @Test
    fun rechazaLoQueNoEsUnPase() {
        listOf(
            "",
            "https://example.com",
            "gymit:v1:ana:1793883630",                       // falta el nonce
            "gymit:v2:ana:1793883630:$nonce",                // versión desconocida
            "otra:v1:ana:1793883630:$nonce",                 // prefijo ajeno
            "gymit:v1:Ana Ruiz:1793883630:$nonce",           // usuario con espacio y mayúsculas
            "gymit:v1:ana:hoy:$nonce",                       // hora que no es un número
            "gymit:v1:ana:-5:$nonce",                        // hora negativa
            "gymit:v1:ana:1793883630:xyz",                   // nonce que no es hexadecimal
            "gymit:v1:ana:1793883630:$nonce:extra"           // campos de más
        ).forEach { assertNull(it, FormatoPase.leer(it)) }
    }
}

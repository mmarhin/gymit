package es.uma.seguridad.gymit.modelo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* ==========================================================================
   MODELADO DE DATOS Y DE ESTADOS
   Teoría: sesión 2.4, apartado 2.4.3 (clases de datos y clases selladas).

   Aquí no hay lógica: sólo la forma de los datos que entran y salen, y los
   estados por los que pasa una pantalla. Es el fichero más corto del proyecto
   y el que más decisiones de seguridad condiciona.
   ========================================================================== */

/** Petición que se envía a POST /api/login */
@Serializable
data class PeticionLogin(
    val usuario: String,
    val clave: String
)

/**
 * Respuesta de POST /api/login.
 *
 * ATENCIÓN — sesión 2.4.3: esta clase guarda un token de sesión y, por ser una
 * clase de datos, el compilador le genera un toString() que incluye TODOS los
 * campos. Si en algún momento se registra este objeto, o aparece en el mensaje
 * de una excepción, el token sale completo en logcat.
 *
 * En Repositorio.kt hay una línea que lo registra a propósito, y en la sección
 * 5.1 de la práctica lo verás con tus propios ojos. En P3.1 lo corregirás.
 */
@Serializable
data class RespuestaLogin(
    val token: String,
    val usuario: String,
    val nombre: String,
    val rol: String
)

/**
 * Una entrada al gimnasio, tal como la devuelve GET /api/elementos.
 *
 * GYMIT: el backend común de la asignatura habla de «elementos»; en nuestro
 * dominio (control de acceso) cada fila es una entrada del socio. Los campos se
 * llaman igual que en el JSON, así que sólo cambia el nombre de la clase.
 *
 * Los nombres de los campos tienen que coincidir con los del JSON del backend:
 * es kotlinx.serialization quien los empareja. Los valores por defecto hacen
 * que un campo que falte no tumbe la aplicación.
 */
@Serializable
data class Entrada(
    val id: Int = 0,
    val titulo: String = "",
    val contenido: String = "",
    val fecha: String = ""
)

/** Envoltorio de la respuesta de GET /api/elementos */
@Serializable
data class RespuestaEntradas(
    // El backend envía la lista bajo la clave "elementos": se mapea al nombre del dominio
    @SerialName("elementos") val entradas: List<Entrada>
)

/**
 * Datos personales devueltos por GET /api/perfil.
 *
 * Fíjate en que aquí hay un DNI. Cuando en la sección 6.2 hagáis el inventario
 * de datos sensibles, éste es uno de los que hay que anotar.
 */
@Serializable
data class Perfil(
    val usuario: String = "",
    val nombre: String = "",
    val correo: String = "",
    val dni: String = "",
    val rol: String = ""
)

/* --------------------------------------------------------------------------
   ESTADOS DE LA INTERFAZ

   Una clase sellada: el compilador conoce TODAS las variantes posibles, así
   que un when que las cubra todas no necesita rama por defecto, y si mañana
   se añade una variante el compilador señala cada sitio que hay que revisar.

   Míralo en acción en ui/PantallaLogin.kt y ui/PantallaLista.kt.

   PARA EL CUADERNO: si añadieras aquí un estado SesionCaducada, ¿qué te diría
   el compilador? ¿Y si en las pantallas se hubiera usado un else genérico?
   -------------------------------------------------------------------------- */

sealed interface EstadoUi<out T> {
    data object Reposo : EstadoUi<Nothing>
    data object Cargando : EstadoUi<Nothing>
    data class Exito<T>(val datos: T) : EstadoUi<T>
    data class Error(val motivo: MotivoError) : EstadoUi<Nothing>
}

/**
 * Los motivos de error.
 *
 * Modelar el fallo como un estado, y no como una excepción suelta, obliga a
 * decidir qué se muestra al usuario en cada caso. Fíjate en que el motivo NO
 * lleva el mensaje crudo de la excepción: en la sesión 2.4.5 vimos que esos
 * mensajes suelen contener rutas, direcciones de servidor y detalles internos
 * que no deben llegar a la pantalla.
 *
 * Fíjate también en que no hay un motivo «UsuarioNoExiste» separado de
 * «ContraseñaIncorrecta». Es deliberado, y el Repositorio explica por qué.
 */
sealed interface MotivoError {
    data object SinConexion : MotivoError
    data object CredencialesNoValidas : MotivoError
    data object SesionCaducada : MotivoError
    data object Desconocido : MotivoError
}

package es.uma.seguridad.gymit.torno.ui

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

/**
 * Analiza cada fotograma de la cámara buscando un QR.
 *
 * Sólo usa el plano Y (la luminancia) de la imagen YUV que entrega CameraX:
 * para leer un QR basta con el blanco y negro, y así no hay que convertir la
 * imagen a color. Se ejecuta en un hilo aparte, nunca en el principal.
 */
class LectorQr(private val alLeer: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val lector = QRCodeReader()
    private val pistas = mapOf(DecodeHintType.TRY_HARDER to true)

    override fun analyze(imagen: ImageProxy) {
        try {
            val plano = imagen.planes[0]
            val buffer = plano.buffer
            val datos = ByteArray(buffer.remaining()).also { buffer.get(it) }
            // rowStride puede ser mayor que el ancho: cada fila trae relleno al final
            val fuente = PlanarYUVLuminanceSource(
                datos, plano.rowStride, imagen.height, 0, 0, imagen.width, imagen.height, false
            )
            alLeer(lector.decode(BinaryBitmap(HybridBinarizer(fuente)), pistas).text)
        } catch (e: ReaderException) {
            // En este fotograma no hay un QR legible: es lo normal la mayor parte del tiempo
        } finally {
            lector.reset()
            imagen.close()
        }
    }
}

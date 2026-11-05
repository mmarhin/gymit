package es.uma.seguridad.gymit.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** La Activity que hay detrás de un Context de Compose, si la hay. */
tailrec fun Context.buscarActividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActividad()
    else -> null
}

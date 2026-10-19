package cl.duoc.activaciones.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formato(patron: String) = SimpleDateFormat(patron, Locale("es", "CL"))

fun Long.aFecha(): String = formato("dd/MM/yyyy").format(Date(this))
fun Long.aFechaHora(): String = formato("dd/MM/yyyy HH:mm").format(Date(this))

/** Devuelve null si el texto no es una fecha dd/MM/yyyy válida. */
fun parsearFecha(texto: String): Long? = runCatching {
    formato("dd/MM/yyyy").apply { isLenient = false }.parse(texto.trim())?.time
}.getOrNull()

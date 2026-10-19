package cl.duoc.activaciones.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Sesion(context: Context) {
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)
    private val _usuarioId = MutableStateFlow(prefs.getLong(CLAVE, -1L).takeIf { it > 0 })
    val usuarioId: StateFlow<Long?> = _usuarioId

    fun iniciar(id: Long) {
        prefs.edit().putLong(CLAVE, id).apply()
        _usuarioId.value = id
    }

    fun cerrar() {
        prefs.edit().remove(CLAVE).apply()
        _usuarioId.value = null
    }

    private companion object { const val CLAVE = "usuarioId" }
}

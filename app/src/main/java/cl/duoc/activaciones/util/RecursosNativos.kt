package cl.duoc.activaciones.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

/**
 * Obtiene la ubicación actual (o la última conocida) con LocationManager.
 * Debe llamarse después de que el usuario concedió el permiso de ubicación.
 * Si solo concedió ubicación aproximada, usa el proveedor de red (el GPS exige permiso fino).
 */
@SuppressLint("MissingPermission")
suspend fun obtenerUbicacion(context: Context): Location? {
    val lm = context.getSystemService(LocationManager::class.java) ?: return null
    val fina = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val proveedor = when {
        fina && lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> return null
    }
    if (Build.VERSION.SDK_INT >= 30) {
        val actual = withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine<Location?> { cont ->
                val senal = CancellationSignal()
                cont.invokeOnCancellation { senal.cancel() }
                lm.getCurrentLocation(proveedor, senal, context.mainExecutor) { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
            }
        }
        if (actual != null) return actual
    }
    return lm.getLastKnownLocation(proveedor)
}

/** Guarda la foto de evidencia en el almacenamiento interno de la app y devuelve su ruta. */
suspend fun guardarFotoEvidencia(context: Context, foto: Bitmap): String = withContext(Dispatchers.IO) {
    val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
    val archivo = File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")
    FileOutputStream(archivo).use { foto.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    archivo.absolutePath
}
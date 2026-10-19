package cl.duoc.activaciones.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cl.duoc.activaciones.ActivacionesApp
import java.util.concurrent.TimeUnit

const val CANAL_ALERTAS = "alertas"

class AlertasWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as ActivacionesApp
        val usuarioId = app.sesion.usuarioId.value ?: return Result.success()
        val alertas = app.repo.alertasUnaVez(usuarioId)
        if (alertas.total == 0) return Result.success()

        val permitido = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!permitido) return Result.success()

        val texto = "${alertas.seguimientosAtrasados.size} seguimientos atrasados · " +
            "${alertas.eventosProximos.size} eventos próximos · " +
            "${alertas.propuestasSinRespuesta.size} propuestas sin respuesta"
        val notificacion = NotificationCompat.Builder(applicationContext, CANAL_ALERTAS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tienes ${alertas.total} alertas pendientes")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(applicationContext).notify(1, notificacion)
        } catch (_: SecurityException) {
        }
        return Result.success()
    }

    companion object {
        fun crearCanal(context: Context) {
            val canal = NotificationChannel(CANAL_ALERTAS, "Alertas", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }

        fun programar(context: Context) {
            val trabajo = PeriodicWorkRequestBuilder<AlertasWorker>(12, TimeUnit.HOURS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("alertas", ExistingPeriodicWorkPolicy.KEEP, trabajo)
        }

        fun ejecutarAhora(context: Context) {
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<AlertasWorker>().build())
        }
    }
}

package cl.duoc.activaciones

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import cl.duoc.activaciones.data.local.AppDatabase
import cl.duoc.activaciones.data.repository.OportunidadRepository
import cl.duoc.activaciones.data.repository.Repositorio
import cl.duoc.activaciones.util.AlertasWorker
import cl.duoc.activaciones.util.Sesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Contenedor manual de dependencias (reemplazable por Hilt). */
class ActivacionesApp : Application() {
    lateinit var repo: Repositorio
        private set
    lateinit var oportunidadRepo: OportunidadRepository
        private set
    lateinit var sesion: Sesion
        private set

    override fun onCreate() {
        super.onCreate()
        limpiarBdObsoleta()
        val db = AppDatabase.crear(this)
        repo = Repositorio(db)
        oportunidadRepo = OportunidadRepository(db)
        sesion = Sesion(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { repo.sembrarSiVacio() }
        AlertasWorker.crearCanal(this)
        AlertasWorker.programar(this)
    }

    /**
     * Borra bases Room de instalaciones previas con versión menor a la actual
     * (evita "A migration from X to N was required but not found").
     * Los datos demo se vuelven a sembrar con sembrarSiVacio().
     */
    private fun limpiarBdObsoleta() {
        databaseList()
            .filterNot { it.endsWith("-journal") || it.endsWith("-wal") || it.endsWith("-shm") }
            .forEach { nombre ->
                val version = runCatching {
                    SQLiteDatabase.openDatabase(getDatabasePath(nombre).path, null, SQLiteDatabase.OPEN_READONLY)
                        .use { it.version }
                }.getOrDefault(VERSION_BD)
                if (version in 1 until VERSION_BD) deleteDatabase(nombre)
            }
    }

    private companion object {
        /** Debe coincidir con @Database(version = ...) en AppDatabase. */
        const val VERSION_BD = 4
    }
}

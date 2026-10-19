package cl.duoc.activaciones.data.repository

import androidx.room.withTransaction
import cl.duoc.activaciones.data.local.AppDatabase
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.data.local.OportunidadEntity
import cl.duoc.activaciones.model.ResultadoActivacion
import cl.duoc.activaciones.model.Seguimiento
import cl.duoc.activaciones.model.TipoAccion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Acceso a datos del módulo de oportunidades. El ViewModel nunca toca el DAO directamente. */
class OportunidadRepository(private val db: AppDatabase) {
    private val dao = db.oportunidadDao()
    private val seguimientos = db.seguimientoDao()
    private val resultados = db.resultadoDao()

    fun oportunidades() = dao.todas()
    fun oportunidad(id: Long) = dao.porId(id)
    suspend fun oportunidadUnaVez(id: Long) = dao.porIdUnaVez(id)
    fun porOrganizacion(orgId: Long) = dao.porOrganizacion(orgId)

    fun todosLosSeguimientos() = seguimientos.todos()
    fun historial(opId: Long) = seguimientos.historial(opId)
    suspend fun agregarSeguimiento(s: Seguimiento) = seguimientos.insertar(s)
    suspend fun eliminarSeguimiento(id: Long) = seguimientos.eliminar(id)

    fun resultado(opId: Long) = resultados.porOportunidad(opId)
    suspend fun guardarResultado(r: ResultadoActivacion) = resultados.guardar(r)

    /** Inserta o actualiza. Si el estado cambió, deja el registro en el historial. */
    suspend fun guardar(o: OportunidadEntity): Long = db.withTransaction {
        val ahora = System.currentTimeMillis()
        if (o.id == 0L) {
            val id = dao.insertar(o)
            seguimientos.insertar(
                Seguimiento(
                    oportunidadId = id, tipoAccion = TipoAccion.CAMBIO_ESTADO, fechaHora = ahora,
                    resultado = "Oportunidad registrada (${o.estado.etiqueta})"
                )
            )
            id
        } else {
            val anterior = dao.porIdUnaVez(o.id)
            dao.actualizar(o)
            if (anterior != null && anterior.estado != o.estado) {
                seguimientos.insertar(
                    Seguimiento(
                        oportunidadId = o.id, tipoAccion = TipoAccion.CAMBIO_ESTADO, fechaHora = ahora,
                        resultado = "${anterior.estado.etiqueta} → ${o.estado.etiqueta}"
                    )
                )
            }
            o.id
        }
    }

    /** Elimina la oportunidad junto con su historial, resultado y foto de evidencia. */
    suspend fun eliminar(o: OportunidadEntity) {
        db.withTransaction {
            seguimientos.eliminarDeOportunidad(o.id)
            resultados.eliminarDeOportunidad(o.id)
            dao.eliminar(o.id)
        }
        o.fotoPath?.let { ruta -> withContext(Dispatchers.IO) { File(ruta).delete() } }
    }

    suspend fun cambiarEstado(o: OportunidadEntity, nuevo: EstadoOportunidad) {
        if (o.estado == nuevo) return
        guardar(o.copy(estado = nuevo, fechaCambioEstado = System.currentTimeMillis()))
    }
}

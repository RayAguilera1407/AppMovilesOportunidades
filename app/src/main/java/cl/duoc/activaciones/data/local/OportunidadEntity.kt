package cl.duoc.activaciones.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.model.Potencial
import cl.duoc.activaciones.model.Prioridad
import cl.duoc.activaciones.model.TipoEvento
import cl.duoc.activaciones.data.local.OportunidadEntity
/**
 * Oportunidad comercial. Los datos del evento asociado (relación 1:1) van en la misma tabla
 * y son opcionales en esta etapa.
 */
@Entity(tableName = "oportunidad", indices = [Index("organizacionId"), Index("responsableId")])
data class OportunidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoEvento,
    val descripcion: String,
    val fuente: String,
    val fechaDeteccion: Long,
    val prioridad: Prioridad,
    val potencial: Potencial,
    val estado: EstadoOportunidad,
    val fechaCambioEstado: Long,
    val observaciones: String = "",
    val organizacionId: Long? = null,
    val responsableId: Long,
    // Evento (opcional)
    val fechaInicio: Long? = null,
    val fechaTermino: Long? = null,
    val horario: String = "",
    val comuna: String = "",
    val region: String = "",
    val publicoEsperado: Int = 0,
    val condiciones: String = "",
    val costo: Int = 0,
    // Recursos nativos
    val referenciaGeografica: String = "",
    val fotoPath: String? = null,
    val codigoQr: String = ""
)
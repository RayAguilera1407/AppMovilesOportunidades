package cl.duoc.activaciones.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Perfil { EJECUTIVO, SUPERVISOR }

enum class EstadoCuenta(val etiqueta: String) { ACTIVA("Activa"), INACTIVA("Inactiva") }

enum class EstadoOportunidad(val etiqueta: String) {
    EVENTO_IDENTIFICADO("Evento identificado"),
    CONTACTO_PENDIENTE("Contacto pendiente"),
    CONTACTO_REALIZADO("Contacto realizado"),
    PROPUESTA_ENVIADA("Propuesta enviada"),
    EN_ESPERA_RESPUESTA("En espera de respuesta"),
    PARTICIPACION_CONFIRMADA("Participación confirmada"),
    DESCARTADA("Descartada")
}

enum class Prioridad(val etiqueta: String) { ALTA("Alta"), MEDIA("Media"), BAJA("Baja") }

enum class Potencial(val etiqueta: String) { ALTO("Alto"), MEDIO("Medio"), BAJO("Bajo") }

enum class TipoEvento(val etiqueta: String) {
    FERIA("Feria"), FERIA_LABORAL("Feria laboral"), SEMINARIO("Seminario"),
    CHARLA("Charla"), ACTIVACION("Activación en terreno"), OTRO("Otro")
}

enum class TipoAccion(val etiqueta: String) {
    LLAMADA("Llamada"), CORREO("Correo"), REUNION("Reunión"), VISITA("Visita"),
    NOTA("Nota"), CAMBIO_ESTADO("Cambio de estado")
}

@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val clave: String,
    val perfil: Perfil,
    val zona: String,
    val area: String = "Comercial",
    val direccion: String = "",
    val estadoCuenta: EstadoCuenta = EstadoCuenta.ACTIVA,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)

@Entity(tableName = "organizacion")
data class Organizacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: String,
    val rubro: String,
    val comuna: String,
    val region: String
)

@Entity(tableName = "contacto", indices = [Index("organizacionId")])
data class Contacto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizacionId: Long,
    val nombre: String,
    val cargo: String,
    val area: String,
    val correo: String,
    val telefono: String
)

@Entity(tableName = "seguimiento", indices = [Index("oportunidadId")])
data class Seguimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    val tipoAccion: TipoAccion,
    val fechaHora: Long,
    val resultado: String,
    val proximaAccion: String = "",
    val fechaProximoSeguimiento: Long? = null
)

@Entity(tableName = "resultado_activacion")
data class ResultadoActivacion(
    @PrimaryKey val oportunidadId: Long,
    val asistentes: Int,
    val contactosObtenidos: Int,
    val inscripciones: Int,
    val evaluacion: String
)
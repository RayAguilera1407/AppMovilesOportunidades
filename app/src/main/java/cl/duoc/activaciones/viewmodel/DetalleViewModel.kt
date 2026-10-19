package cl.duoc.activaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.activaciones.data.local.OportunidadEntity
import cl.duoc.activaciones.data.repository.OportunidadRepository
import cl.duoc.activaciones.data.repository.Repositorio
import cl.duoc.activaciones.model.Contacto
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.model.Organizacion
import cl.duoc.activaciones.model.ResultadoActivacion
import cl.duoc.activaciones.model.Seguimiento
import cl.duoc.activaciones.model.Usuario
import cl.duoc.activaciones.model.visibles
import cl.duoc.activaciones.util.Sesion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModel(
    private val repo: Repositorio,
    private val oportunidadRepo: OportunidadRepository,
    sesion: Sesion,
    private val id: Long
) : ViewModel() {

    private fun <T> Flow<T>.estado(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), inicial)

    private val usuario: StateFlow<Usuario?> = sesion.usuarioId
        .flatMapLatest { uid -> if (uid == null) flowOf<Usuario?>(null) else repo.usuario(uid) }
        .estado(null)

    /** Si la oportunidad no es del usuario (y no es supervisor), llega null. */
    val oportunidad: StateFlow<OportunidadEntity?> =
        combine(oportunidadRepo.oportunidad(id), usuario) { o, u ->
            o?.takeIf { visibles(listOf(it), u).isNotEmpty() }
        }.estado(null)

    val organizacion: StateFlow<Organizacion?> =
        oportunidad.flatMapLatest { o ->
            val orgId = o?.organizacionId
            if (orgId == null) flowOf<Organizacion?>(null) else repo.organizacion(orgId)
        }.estado(null)

    val contactos: StateFlow<List<Contacto>> =
        oportunidad.flatMapLatest { o ->
            val orgId = o?.organizacionId
            if (orgId == null) flowOf(emptyList<Contacto>()) else repo.contactos(orgId)
        }.estado(emptyList())

    val historial: StateFlow<List<Seguimiento>> =
        oportunidadRepo.historial(id).estado(emptyList())

    val resultado: StateFlow<ResultadoActivacion?> =
        oportunidadRepo.resultado(id).estado(null)

    fun cambiarEstado(nuevo: EstadoOportunidad) {
        val actual = oportunidad.value ?: return
        viewModelScope.launch { oportunidadRepo.cambiarEstado(actual, nuevo) }
    }

    fun agregarSeguimiento(s: Seguimiento) {
        if (oportunidad.value == null) return
        viewModelScope.launch { oportunidadRepo.agregarSeguimiento(s.copy(oportunidadId = id)) }
    }

    fun eliminarSeguimiento(s: Seguimiento) {
        if (oportunidad.value == null) return
        viewModelScope.launch { oportunidadRepo.eliminarSeguimiento(s.id) }
    }

    fun guardarResultado(r: ResultadoActivacion) {
        if (oportunidad.value == null) return
        viewModelScope.launch { oportunidadRepo.guardarResultado(r.copy(oportunidadId = id)) }
    }

    fun eliminar(alTerminar: () -> Unit) {
        val actual = oportunidad.value ?: return
        viewModelScope.launch {
            oportunidadRepo.eliminar(actual)
            alTerminar()
        }
    }
}
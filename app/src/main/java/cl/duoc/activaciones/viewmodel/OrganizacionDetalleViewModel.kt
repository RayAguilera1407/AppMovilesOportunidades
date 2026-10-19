package cl.duoc.activaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.activaciones.data.local.OportunidadEntity
import cl.duoc.activaciones.data.repository.OportunidadRepository
import cl.duoc.activaciones.data.repository.Repositorio
import cl.duoc.activaciones.model.Contacto
import cl.duoc.activaciones.model.Organizacion
import cl.duoc.activaciones.model.Usuario
import cl.duoc.activaciones.model.visibles
import cl.duoc.activaciones.util.Sesion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizacionDetalleViewModel(
    private val repo: Repositorio,
    oportunidadRepo: OportunidadRepository,
    sesion: Sesion,
    private val id: Long
) : ViewModel() {

    private val usuario: StateFlow<Usuario?> = sesion.usuarioId
        .flatMapLatest { uid -> if (uid == null) flowOf<Usuario?>(null) else repo.usuario(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val organizacion: StateFlow<Organizacion?> =
        repo.organizacion(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contactos: StateFlow<List<Contacto>> =
        repo.contactos(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** El ejecutivo solo ve sus oportunidades; el supervisor ve todas. */
    val eventos: StateFlow<List<OportunidadEntity>> =
        combine(oportunidadRepo.porOrganizacion(id), usuario) { ops, u -> visibles(ops, u) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** La pantalla manda organizacionId = 0; aquí se asigna el real. */
    fun agregarContacto(c: Contacto) {
        viewModelScope.launch { repo.guardarContacto(c.copy(organizacionId = id)) }
    }

    fun eliminarContacto(c: Contacto) {
        viewModelScope.launch { repo.eliminarContacto(c.id) }
    }
}
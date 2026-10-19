package cl.duoc.activaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.activaciones.data.repository.Repositorio
import cl.duoc.activaciones.model.Organizacion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrganizacionesViewModel(private val repo: Repositorio) : ViewModel() {

    val organizaciones: StateFlow<List<Organizacion>> =
        repo.organizaciones()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun crear(o: Organizacion) {
        viewModelScope.launch { repo.guardarOrganizacion(o) }
    }

    fun eliminar(o: Organizacion) {
        viewModelScope.launch { repo.eliminarOrganizacion(o.id) }
    }
}
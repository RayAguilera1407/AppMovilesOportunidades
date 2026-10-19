package cl.duoc.activaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.activaciones.data.repository.OportunidadRepository
import cl.duoc.activaciones.data.repository.Repositorio
import cl.duoc.activaciones.model.Alertas
import cl.duoc.activaciones.model.Usuario
import cl.duoc.activaciones.model.calcularAlertas
import cl.duoc.activaciones.model.visibles
import cl.duoc.activaciones.util.Sesion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class PrincipalViewModel(
    repo: Repositorio,
    oportunidadRepo: OportunidadRepository,
    sesion: Sesion
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf<Usuario?>(null) else repo.usuario(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val alertas: StateFlow<Alertas> = combine(
        oportunidadRepo.oportunidades(),
        oportunidadRepo.todosLosSeguimientos(),
        usuario
    ) { ops, segs, u ->
        calcularAlertas(visibles(ops, u), segs, System.currentTimeMillis())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Alertas())
}
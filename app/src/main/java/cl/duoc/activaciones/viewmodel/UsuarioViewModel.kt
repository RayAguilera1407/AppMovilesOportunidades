package cl.duoc.activaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.activaciones.data.repository.OportunidadRepository
import cl.duoc.activaciones.data.repository.Repositorio
import cl.duoc.activaciones.model.Alertas
import cl.duoc.activaciones.model.Perfil
import cl.duoc.activaciones.model.Usuario
import cl.duoc.activaciones.model.UsuarioUiState
import cl.duoc.activaciones.model.calcularAlertas
import cl.duoc.activaciones.model.validarRegistro
import cl.duoc.activaciones.model.visibles
import cl.duoc.activaciones.util.Sesion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModel(
    private val repo: Repositorio,
    private val oportunidadRepo: OportunidadRepository,
    private val sesion: Sesion
) : ViewModel() {

    // ---------- Sesión / perfil ----------

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

    fun cerrarSesion() {
        sesion.cerrar()
    }

    // ---------- Registro ----------

    private val _estado = MutableStateFlow(UsuarioUiState())
    val estado: StateFlow<UsuarioUiState> = _estado.asStateFlow()

    /** Último usuario creado: lo lee ResumenScreen (el formulario se limpia al registrar). */
    private val _ultimoRegistro = MutableStateFlow<Usuario?>(null)
    val ultimoRegistro: StateFlow<Usuario?> = _ultimoRegistro.asStateFlow()

    // Cada cambio limpia el error de su campo
    fun onNombreChange(v: String) = _estado.update { it.copy(nombre = v, errores = it.errores.copy(nombre = null)) }
    fun onCorreoChange(v: String) = _estado.update { it.copy(correo = v, errores = it.errores.copy(correo = null)) }
    fun onClaveChange(v: String) = _estado.update {
        it.copy(clave = v, errores = it.errores.copy(clave = null, confirmarClave = null))
    }
    fun onConfirmarClaveChange(v: String) = _estado.update {
        it.copy(confirmarClave = v, errores = it.errores.copy(confirmarClave = null))
    }
    fun onDireccionChange(v: String) = _estado.update {
        it.copy(direccion = v, errores = it.errores.copy(direccion = null))
    }
    fun onAreaChange(v: String) = _estado.update { it.copy(area = v, errores = it.errores.copy(area = null)) }
    fun onZonaChange(v: String) = _estado.update { it.copy(zona = v, errores = it.errores.copy(zona = null)) }
    fun onAceptarTerminosChange(v: Boolean) = _estado.update {
        it.copy(aceptaTerminos = v, errores = it.errores.copy(terminos = null))
    }

    fun limpiar() {
        _estado.value = UsuarioUiState()
    }

    /** Valida todo el formulario, deja los errores en el estado y devuelve true si es válido. */
    fun validarFormulario(): Boolean {
        val errores = validarRegistro(_estado.value)
        _estado.update { it.copy(errores = errores) }
        return !errores.hayErrores
    }

    fun registrar(alExito: () -> Unit) {
        val s = _estado.value
        if (s.registrando) return
        if (!validarFormulario()) return

        val area = s.area ?: return
        val zona = s.zona ?: return
        _estado.update { it.copy(registrando = true) }

        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            val nuevo = Usuario(
                nombre = s.nombre.trim(),
                correo = s.correo.trim().lowercase(),
                clave = s.clave, // el repositorio la guarda con hash
                perfil = Perfil.EJECUTIVO, // el registro nunca crea supervisores
                zona = zona,
                area = area,
                direccion = s.direccion.trim(),
                fechaCreacion = ahora,
                fechaActualizacion = ahora
            )
            if (repo.registrar(nuevo)) {
                _ultimoRegistro.value = nuevo
                _estado.value = UsuarioUiState()
                alExito()
            } else {
                _estado.update {
                    it.copy(
                        registrando = false,
                        errores = it.errores.copy(correo = "Este correo ya está registrado")
                    )
                }
            }
        }
    }
}
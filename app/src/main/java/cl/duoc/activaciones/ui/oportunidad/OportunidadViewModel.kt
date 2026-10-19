package cl.duoc.activaciones.ui.oportunidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.activaciones.data.local.OportunidadEntity
import cl.duoc.activaciones.data.repository.OportunidadRepository
import cl.duoc.activaciones.data.repository.Repositorio
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.model.Filtro
import cl.duoc.activaciones.model.Organizacion
import cl.duoc.activaciones.model.Potencial
import cl.duoc.activaciones.model.Prioridad
import cl.duoc.activaciones.model.TipoEvento
import cl.duoc.activaciones.model.Usuario
import cl.duoc.activaciones.model.errorEnteroPositivo
import cl.duoc.activaciones.model.filtrar
import cl.duoc.activaciones.model.visibles
import cl.duoc.activaciones.util.Sesion
import cl.duoc.activaciones.util.aFecha
import cl.duoc.activaciones.util.parsearFecha
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/** Estado del formulario: valores, errores por campo y estado de guardado. */
data class OportunidadUiState(
    val nombre: String = "",
    val tipo: TipoEvento? = null,
    val descripcion: String = "",
    val fuente: String = "",
    val prioridad: Prioridad? = null,
    val potencial: Potencial? = null,
    val estado: EstadoOportunidad = EstadoOportunidad.EVENTO_IDENTIFICADO,
    val observaciones: String = "",
    val organizacionId: Long? = null,
    // Evento (opcional)
    val fechaInicio: String = "",
    val fechaTermino: String = "",
    val horario: String = "",
    val comuna: String = "",
    val region: String = "",
    val publico: String = "",
    val condiciones: String = "",
    val costo: String = "",
    // Recursos nativos
    val referenciaGeografica: String = "",
    val fotoPath: String? = null,
    val codigoQr: String = "",
    val mensajeRecurso: String? = null,
    // Errores
    val nombreError: String? = null,
    val tipoError: String? = null,
    val descripcionError: String? = null,
    val fuenteError: String? = null,
    val prioridadError: String? = null,
    val potencialError: String? = null,
    val fechaError: String? = null,
    val publicoError: String? = null,
    val costoError: String? = null,
    val comunaError: String? = null,
    // Guardado
    val esEdicion: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
) {
    val tieneErrores: Boolean
        get() = listOf(
            nombreError, tipoError, descripcionError, fuenteError, prioridadError,
            potencialError, fechaError, publicoError, costoError, comunaError
        ).any { it != null }
}

/** ViewModel del módulo de oportunidades: lista (con filtros) y formulario crear/editar. */
@OptIn(ExperimentalCoroutinesApi::class)
class OportunidadViewModel(
    private val oportunidadRepo: OportunidadRepository,
    private val repo: Repositorio,
    private val sesion: Sesion
) : ViewModel() {

    private fun <T> Flow<T>.estado(scope: CoroutineScope, inicial: T): StateFlow<T> =
        stateIn(scope, SharingStarted.WhileSubscribed(5000), inicial)

    // ---------- Lista ----------
    val usuario: StateFlow<Usuario?> = sesion.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf<Usuario?>(null) else repo.usuario(id) }
        .estado(viewModelScope, null)

    val usuarios: StateFlow<List<Usuario>> = repo.usuarios().estado(viewModelScope, emptyList())
    val organizaciones: StateFlow<List<Organizacion>> = repo.organizaciones().estado(viewModelScope, emptyList())

    private val _filtro = MutableStateFlow(Filtro())
    val filtro: StateFlow<Filtro> = _filtro.asStateFlow()

    private val propias: Flow<List<OportunidadEntity>> =
        combine(oportunidadRepo.oportunidades(), usuario) { ops, u -> visibles(ops, u) }

    /** Se recalcula sola cuando Room emite cambios: la lista se actualiza al registrar o editar. */
    val lista: StateFlow<List<OportunidadEntity>> =
        combine(propias, _filtro) { ops, f -> filtrar(ops, f) }.estado(viewModelScope, emptyList())

    val regiones: StateFlow<List<String>> = propias
        .map { ops -> ops.map { it.region }.filter { it.isNotBlank() }.distinct().sorted() }
        .estado(viewModelScope, emptyList())

    fun actualizarFiltro(f: Filtro) {
        _filtro.value = f
    }

    fun eliminar(o: OportunidadEntity) {
        viewModelScope.launch { oportunidadRepo.eliminar(o) }
    }

    // ---------- Formulario ----------
    private val _uiState = MutableStateFlow(OportunidadUiState())
    val uiState: StateFlow<OportunidadUiState> = _uiState.asStateFlow()

    private var original: OportunidadEntity? = null
    private var idCargado: Long? = null

    fun cargar(id: Long) {
        if (idCargado == id) return
        idCargado = id
        if (id <= 0) {
            original = null
            _uiState.value = OportunidadUiState()
            return
        }
        viewModelScope.launch {
            val o = oportunidadRepo.oportunidadUnaVez(id) ?: return@launch
            // Mínimo privilegio: un ejecutivo no puede editar oportunidades ajenas
            val usuarioActual = sesion.usuarioId.value?.let { repo.usuarioUnaVez(it) }
            if (visibles(listOf(o), usuarioActual).isEmpty()) return@launch

            original = o
            _uiState.value = OportunidadUiState(
                nombre = o.nombre, tipo = o.tipo, descripcion = o.descripcion, fuente = o.fuente,
                prioridad = o.prioridad, potencial = o.potencial, estado = o.estado,
                observaciones = o.observaciones, organizacionId = o.organizacionId,
                fechaInicio = o.fechaInicio?.aFecha() ?: "", fechaTermino = o.fechaTermino?.aFecha() ?: "",
                horario = o.horario, comuna = o.comuna, region = o.region,
                publico = if (o.publicoEsperado > 0) o.publicoEsperado.toString() else "",
                condiciones = o.condiciones, costo = if (o.costo > 0) o.costo.toString() else "",
                referenciaGeografica = o.referenciaGeografica, fotoPath = o.fotoPath,
                codigoQr = o.codigoQr,
                esEdicion = true
            )
        }
    }

    // Cada cambio limpia el error de su campo
    fun onNombre(v: String) = _uiState.update { it.copy(nombre = v, nombreError = null) }
    fun onTipo(v: TipoEvento) = _uiState.update { it.copy(tipo = v, tipoError = null) }
    fun onDescripcion(v: String) = _uiState.update { it.copy(descripcion = v, descripcionError = null) }
    fun onFuente(v: String) = _uiState.update { it.copy(fuente = v, fuenteError = null) }
    fun onPrioridad(v: Prioridad) = _uiState.update { it.copy(prioridad = v, prioridadError = null) }
    fun onPotencial(v: Potencial) = _uiState.update { it.copy(potencial = v, potencialError = null) }
    fun onEstado(v: EstadoOportunidad) = _uiState.update { it.copy(estado = v) }
    fun onObservaciones(v: String) = _uiState.update { it.copy(observaciones = v) }
    fun onOrganizacion(v: Long?) = _uiState.update { it.copy(organizacionId = v) }
    fun onFechaInicio(v: String) = _uiState.update { it.copy(fechaInicio = v, fechaError = null) }
    fun onFechaTermino(v: String) = _uiState.update { it.copy(fechaTermino = v, fechaError = null) }
    fun onHorario(v: String) = _uiState.update { it.copy(horario = v) }

    // Si cambia la región, la comuna anterior ya no es válida
    fun onRegion(v: String) = _uiState.update {
        if (it.region == v) it else it.copy(region = v, comuna = "", comunaError = null)
    }
    fun onComuna(v: String) = _uiState.update { it.copy(comuna = v, comunaError = null) }

    fun onPublico(v: String) = _uiState.update { it.copy(publico = v, publicoError = null) }
    fun onCondiciones(v: String) = _uiState.update { it.copy(condiciones = v) }
    fun onCosto(v: String) = _uiState.update { it.copy(costo = v, costoError = null) }

    fun onUbicacion(latitud: Double?, longitud: Double?) = _uiState.update {
        if (latitud == null || longitud == null) {
            it.copy(mensajeRecurso = "No se pudo obtener la ubicación. Activa el GPS e inténtalo de nuevo.")
        } else {
            it.copy(
                referenciaGeografica = String.format(Locale.US, "Lat %.5f, Lng %.5f", latitud, longitud),
                mensajeRecurso = "Ubicación registrada"
            )
        }
    }

    fun onFoto(path: String) = _uiState.update { it.copy(fotoPath = path, mensajeRecurso = "Foto de evidencia guardada") }

    // Se llama cuando vuelve el resultado del escáner QR
    fun onQr(contenido: String) = _uiState.update { it.copy(codigoQr = contenido, mensajeRecurso = "Código QR registrado") }

    fun onMensajeRecurso(mensaje: String) = _uiState.update { it.copy(mensajeRecurso = mensaje) }

    /** Valida en el ViewModel; la vista solo muestra los errores que quedan en el UiState. */
    fun guardar() {
        val s = _uiState.value
        if (s.guardando) return
        val inicio = if (s.fechaInicio.isBlank()) null else parsearFecha(s.fechaInicio)
        val termino = if (s.fechaTermino.isBlank()) inicio else parsearFecha(s.fechaTermino)

        val validado = s.copy(
            nombreError = when {
                s.nombre.isBlank() -> "El nombre es obligatorio"
                s.nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
                else -> null
            },
            tipoError = if (s.tipo == null) "Selecciona el tipo de oportunidad" else null,
            descripcionError = if (s.descripcion.isBlank()) "La descripción es obligatoria" else null,
            fuenteError = if (s.fuente.isBlank()) "Indica la fuente de información" else null,
            prioridadError = if (s.prioridad == null) "Selecciona la prioridad" else null,
            potencialError = if (s.potencial == null) "Selecciona el potencial comercial" else null,
            fechaError = when {
                s.fechaInicio.isNotBlank() && inicio == null -> "Fecha de inicio inválida (dd/MM/aaaa)"
                s.fechaTermino.isNotBlank() && termino == null -> "Fecha de término inválida (dd/MM/aaaa)"
                inicio != null && termino != null && termino < inicio -> "El término no puede ser anterior al inicio"
                else -> null
            },
            publicoError = errorEnteroPositivo(s.publico),
            costoError = errorEnteroPositivo(s.costo),
            comunaError = if (s.region.isNotBlank() && s.comuna.isBlank()) "Selecciona la comuna" else null
        )
        _uiState.value = validado
        if (validado.tieneErrores) return

        val usuarioId = sesion.usuarioId.value ?: return
        val tipo = s.tipo ?: return
        val prioridad = s.prioridad ?: return
        val potencial = s.potencial ?: return
        _uiState.update { it.copy(guardando = true) }

        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            val base = original
            val entidad = OportunidadEntity(
                id = base?.id ?: 0,
                nombre = s.nombre.trim(),
                tipo = tipo,
                descripcion = s.descripcion.trim(),
                fuente = s.fuente.trim(),
                fechaDeteccion = base?.fechaDeteccion ?: ahora,
                prioridad = prioridad,
                potencial = potencial,
                estado = s.estado,
                fechaCambioEstado = if (base == null || base.estado != s.estado) ahora else base.fechaCambioEstado,
                observaciones = s.observaciones.trim(),
                organizacionId = s.organizacionId,
                responsableId = base?.responsableId ?: usuarioId,
                fechaInicio = inicio,
                fechaTermino = termino,
                horario = s.horario.trim(),
                comuna = s.comuna.trim(),
                region = s.region.trim(),
                publicoEsperado = s.publico.toIntOrNull() ?: 0,
                condiciones = s.condiciones.trim(),
                costo = s.costo.toIntOrNull() ?: 0,
                referenciaGeografica = s.referenciaGeografica,
                fotoPath = s.fotoPath,
                codigoQr = s.codigoQr
            )
            oportunidadRepo.guardar(entidad)
            _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
        }
    }
}
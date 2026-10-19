package cl.duoc.activaciones.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.model.ResultadoActivacion
import cl.duoc.activaciones.model.Seguimiento
import cl.duoc.activaciones.model.TipoAccion
import cl.duoc.activaciones.model.DIA_MS
import cl.duoc.activaciones.viewmodel.DetalleViewModel
import cl.duoc.activaciones.ui.components.Campo
import cl.duoc.activaciones.ui.components.ConfirmarEliminacion
import cl.duoc.activaciones.ui.components.EstadoChip
import cl.duoc.activaciones.ui.components.Selector
import cl.duoc.activaciones.ui.theme.barraColores
import cl.duoc.activaciones.ui.components.app
import cl.duoc.activaciones.ui.components.vmFactory
import cl.duoc.activaciones.ui.oportunidad.FotoEvidencia
import cl.duoc.activaciones.util.aFecha
import cl.duoc.activaciones.util.aFechaHora

@Composable
fun DetalleScreen(id: Long, alVolver: () -> Unit, alEditar: (Long) -> Unit) {
    val app = app()
    val vm: DetalleViewModel = viewModel(factory = vmFactory { DetalleViewModel(app.repo, app.oportunidadRepo, app.sesion, id) })
    val oportunidad by vm.oportunidad.collectAsState()
    val organizacion by vm.organizacion.collectAsState()
    val contactos by vm.contactos.collectAsState()
    val historial by vm.historial.collectAsState()
    val resultado by vm.resultado.collectAsState()
    var dialogoSeguimiento by remember { mutableStateOf(false) }
    var dialogoResultado by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }
    var seguimientoPorEliminar by remember { mutableStateOf<Seguimiento?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = barraColores(),
                title = { Text("Detalle de oportunidad") },
                navigationIcon = { IconButton(onClick = alVolver) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") } },
                actions = {
                    IconButton(onClick = { alEditar(id) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                    IconButton(onClick = { confirmarEliminar = true }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
                }
            )
        }
    ) { pad ->
        val o = oportunidad
        if (o == null) {
            Text("Cargando…", Modifier.padding(pad).padding(16.dp))
        } else LazyColumn(
            Modifier.padding(pad).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(o.nombre, style = MaterialTheme.typography.headlineSmall)
                EstadoChip(o.estado)
            }
            item {
                Seccion("Oportunidad") {
                    Dato("Tipo", o.tipo.etiqueta)
                    Dato("Descripción", o.descripcion)
                    Dato("Fuente", o.fuente)
                    Dato("Detectada", o.fechaDeteccion.aFecha())
                    Dato("Prioridad", o.prioridad.etiqueta)
                    Dato("Potencial comercial", o.potencial.etiqueta)
                    if (o.observaciones.isNotBlank()) Dato("Observaciones", o.observaciones)
                }
            }
            item {
                Seccion("Evento") {
                    Dato("Fechas", o.fechaInicio?.let { "${it.aFecha()} al ${(o.fechaTermino ?: it).aFecha()}" } ?: "Por definir")
                    if (o.horario.isNotBlank()) Dato("Horario", o.horario)
                    Dato("Lugar", listOf(o.comuna, o.region).filter { it.isNotBlank() }.joinToString(", ").ifEmpty { "Por definir" })
                    Dato("Público esperado", o.publicoEsperado.toString())
                    Dato("Condiciones", o.condiciones)
                    Dato("Costo", "$${o.costo}")
                }
            }
            // QR: la sección también aparece si hay un código QR guardado
            if (o.referenciaGeografica.isNotBlank() || o.fotoPath != null || o.codigoQr.isNotBlank()) {
                item {
                    Seccion("Evidencias") {
                        if (o.referenciaGeografica.isNotBlank()) Dato("Referencia geográfica", o.referenciaGeografica)
                        if (o.codigoQr.isNotBlank()) Dato("Código QR", o.codigoQr) // QR
                        FotoEvidencia(o.fotoPath)
                    }
                }
            }
            item {
                Seccion("Organización y contactos") {
                    if (organizacion == null) Dato("Organización", "Sin asignar")
                    organizacion?.let { Dato(it.nombre, "${it.tipo} · ${it.rubro}") }
                    contactos.forEach { c -> Dato(c.nombre, "${c.cargo} (${c.area}) · ${c.correo} · ${c.telefono}") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Selector("Estado", EstadoOportunidad.values().toList(), o.estado, { it.etiqueta }) { vm.cambiarEstado(it) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { dialogoSeguimiento = true }) { Text("Registrar seguimiento") }
                    if (o.estado == EstadoOportunidad.PARTICIPACION_CONFIRMADA) {
                        OutlinedButton(onClick = { dialogoResultado = true }) { Text("Resultado") }
                    }
                }
            }
            resultado?.let { r ->
                item {
                    Seccion("Resultado de la activación") {
                        Dato("Asistentes", r.asistentes.toString())
                        Dato("Contactos obtenidos", r.contactosObtenidos.toString())
                        Dato("Inscripciones", r.inscripciones.toString())
                        Dato("Evaluación", r.evaluacion)
                    }
                }
            }
            item { Text("Historial", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
            items(historial, key = { it.id }) { s ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${s.tipoAccion.etiqueta} · ${s.fechaHora.aFechaHora()}", fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            if (s.tipoAccion != TipoAccion.CAMBIO_ESTADO) {
                                IconButton(onClick = { seguimientoPorEliminar = s }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar seguimiento", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Text(s.resultado)
                        if (s.proximaAccion.isNotBlank()) {
                            Text(
                                "Próxima acción: ${s.proximaAccion}" + (s.fechaProximoSeguimiento?.let { " (${it.aFecha()})" } ?: ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            item { Text("") }
        }
    }

    if (confirmarEliminar) {
        ConfirmarEliminacion(
            titulo = "Eliminar oportunidad",
            mensaje = "Se eliminará la oportunidad con su historial, resultado y evidencias. Esta acción no se puede deshacer.",
            alCancelar = { confirmarEliminar = false },
            alConfirmar = {
                confirmarEliminar = false
                vm.eliminar(alTerminar = alVolver)
            }
        )
    }
    seguimientoPorEliminar?.let { s ->
        ConfirmarEliminacion(
            titulo = "Eliminar seguimiento",
            mensaje = "¿Eliminar el registro \"${s.resultado}\" del historial?",
            alCancelar = { seguimientoPorEliminar = null },
            alConfirmar = {
                vm.eliminarSeguimiento(s)
                seguimientoPorEliminar = null
            }
        )
    }
    if (dialogoSeguimiento) {
        DialogoSeguimiento(alCerrar = { dialogoSeguimiento = false }) { vm.agregarSeguimiento(it) }
    }
    if (dialogoResultado) {
        DialogoResultado(resultado, alCerrar = { dialogoResultado = false }) { vm.guardarResultado(it) }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            contenido()
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Text("$etiqueta: $valor", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun DialogoSeguimiento(alCerrar: () -> Unit, alGuardar: (Seguimiento) -> Unit) {
    val tipos = TipoAccion.values().filter { it != TipoAccion.CAMBIO_ESTADO }
    var tipo by remember { mutableStateOf(TipoAccion.LLAMADA) }
    var resultado by remember { mutableStateOf("") }
    var proxima by remember { mutableStateOf("") }
    var dias by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Nuevo seguimiento") },
        text = {
            Column {
                Selector("Acción", tipos, tipo, { it.etiqueta }) { tipo = it }
                Campo("Resultado", resultado, unaLinea = false) { resultado = it }
                Campo("Próxima acción", proxima) { proxima = it }
                Campo("Próximo seguimiento en (días)", dias, numerico = true) { dias = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = resultado.isNotBlank(),
                onClick = {
                    val ahora = System.currentTimeMillis()
                    alGuardar(
                        Seguimiento(
                            oportunidadId = 0, tipoAccion = tipo, fechaHora = ahora, resultado = resultado.trim(),
                            proximaAccion = proxima.trim(),
                            fechaProximoSeguimiento = dias.toIntOrNull()?.let { ahora + it * DIA_MS }
                        )
                    )
                    alCerrar()
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoResultado(actual: ResultadoActivacion?, alCerrar: () -> Unit, alGuardar: (ResultadoActivacion) -> Unit) {
    var asistentes by remember { mutableStateOf(actual?.asistentes?.toString() ?: "") }
    var contactos by remember { mutableStateOf(actual?.contactosObtenidos?.toString() ?: "") }
    var inscripciones by remember { mutableStateOf(actual?.inscripciones?.toString() ?: "") }
    var evaluacion by remember { mutableStateOf(actual?.evaluacion ?: "") }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Resultado de la activación") },
        text = {
            Column {
                Campo("Asistentes", asistentes, numerico = true) { asistentes = it }
                Campo("Contactos obtenidos", contactos, numerico = true) { contactos = it }
                Campo("Inscripciones", inscripciones, numerico = true) { inscripciones = it }
                Campo("Evaluación", evaluacion, unaLinea = false) { evaluacion = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                alGuardar(
                    ResultadoActivacion(
                        oportunidadId = 0, asistentes = asistentes.toIntOrNull() ?: 0,
                        contactosObtenidos = contactos.toIntOrNull() ?: 0,
                        inscripciones = inscripciones.toIntOrNull() ?: 0, evaluacion = evaluacion.trim()
                    )
                )
                alCerrar()
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
    )
}
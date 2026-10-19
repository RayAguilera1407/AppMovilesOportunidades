package cl.duoc.activaciones.ui.oportunidad

import android.Manifest
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.model.Potencial
import cl.duoc.activaciones.model.Prioridad
import cl.duoc.activaciones.model.REGIONES
import cl.duoc.activaciones.model.TipoEvento
import cl.duoc.activaciones.model.comunasDe
import cl.duoc.activaciones.ui.components.Campo
import cl.duoc.activaciones.ui.components.CampoSelector
import cl.duoc.activaciones.ui.components.app
import cl.duoc.activaciones.ui.components.vmFactory
import cl.duoc.activaciones.ui.theme.barraColores
import cl.duoc.activaciones.util.guardarFotoEvidencia
import cl.duoc.activaciones.util.obtenerUbicacion
import kotlinx.coroutines.launch

/** Registrar (id <= 0) o modificar una oportunidad. Toda la lógica vive en OportunidadViewModel. */
@Composable
fun OportunidadFormScreen(
    id: Long,
    alVolver: () -> Unit,
    alEscanearQr: () -> Unit = {},
    qrEscaneado: String? = null,
    alConsumirQr: () -> Unit = {}
) {
    val app = app()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vm: OportunidadViewModel = viewModel(
        factory = vmFactory { OportunidadViewModel(app.oportunidadRepo, app.repo, app.sesion) }
    )
    val estado by vm.uiState.collectAsState()
    val organizaciones by vm.organizaciones.collectAsState()

    LaunchedEffect(id) { vm.cargar(id) }
    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente) alVolver()
    }
    // Al volver del escáner, guarda el texto en el formulario
    LaunchedEffect(qrEscaneado) {
        qrEscaneado?.let {
            vm.onQr(it)
            alConsumirQr()
        }
    }

    // ----- Recurso nativo: ubicación -----
    val permisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) {
            scope.launch {
                val loc = obtenerUbicacion(context)
                vm.onUbicacion(loc?.latitude, loc?.longitude)
            }
        } else {
            vm.onMensajeRecurso("Permiso de ubicación denegado")
        }
    }

    // ----- Recurso nativo: cámara -----
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            scope.launch { vm.onFoto(guardarFotoEvidencia(context, bitmap)) }
        }
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) tomarFoto.launch(null) else vm.onMensajeRecurso("Permiso de cámara denegado")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = barraColores(),
                title = { Text(if (estado.esEdicion) "Editar oportunidad" else "Nueva oportunidad") },
                navigationIcon = { IconButton(onClick = alVolver) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Titulo("Datos de la oportunidad")
            Campo("Nombre de la oportunidad *", estado.nombre, error = estado.nombreError, onCambio = vm::onNombre)
            CampoSelector(
                "Tipo de oportunidad *", TipoEvento.values().toList(), estado.tipo, { it.etiqueta },
                error = estado.tipoError, onSeleccion = vm::onTipo
            )
            Campo("Descripción *", estado.descripcion, unaLinea = false, error = estado.descripcionError, onCambio = vm::onDescripcion)
            Campo("Fuente de información *", estado.fuente, error = estado.fuenteError, onCambio = vm::onFuente)
            CampoSelector(
                "Prioridad *", Prioridad.values().toList(), estado.prioridad, { it.etiqueta },
                error = estado.prioridadError, onSeleccion = vm::onPrioridad
            )
            CampoSelector(
                "Potencial comercial *", Potencial.values().toList(), estado.potencial, { it.etiqueta },
                error = estado.potencialError, onSeleccion = vm::onPotencial
            )
            CampoSelector(
                "Estado de gestión *", EstadoOportunidad.values().toList(), estado.estado, { it.etiqueta },
                onSeleccion = vm::onEstado
            )
            Campo("Observaciones", estado.observaciones, unaLinea = false, onCambio = vm::onObservaciones)
            CampoSelector(
                "Organización (opcional)", listOf<Long?>(null) + organizaciones.map { it.id }, estado.organizacionId,
                { sel -> organizaciones.firstOrNull { it.id == sel }?.nombre ?: "Sin organización" },
                onSeleccion = vm::onOrganizacion
            )

            Titulo("Evento asociado (opcional)")
            Campo("Fecha inicio (dd/MM/aaaa)", estado.fechaInicio, error = estado.fechaError, onCambio = vm::onFechaInicio)
            Campo("Fecha término (dd/MM/aaaa)", estado.fechaTermino, onCambio = vm::onFechaTermino)
            Campo("Horario", estado.horario, onCambio = vm::onHorario)
            CampoSelector(
                "Región", REGIONES, estado.region.ifBlank { null }, { it },
                onSeleccion = vm::onRegion
            )
            CampoSelector(
                "Comuna", comunasDe(estado.region), estado.comuna.ifBlank { null }, { it },
                error = estado.comunaError,
                habilitado = estado.region.isNotBlank(),
                onSeleccion = vm::onComuna
            )
            Campo("Público esperado", estado.publico, numerico = true, error = estado.publicoError, onCambio = vm::onPublico)
            Campo("Condiciones de participación", estado.condiciones, unaLinea = false, onCambio = vm::onCondiciones)
            Campo("Costo asociado", estado.costo, numerico = true, error = estado.costoError, onCambio = vm::onCosto)

            Titulo("Recursos del dispositivo")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            permisoUbicacion.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Text("  Obtener referencia geográfica")
                    }
                    Text(
                        if (estado.referenciaGeografica.isBlank()) "Sin ubicación registrada" else estado.referenciaGeografica,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = { permisoCamara.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Tomar foto de evidencia") }
                    FotoEvidencia(estado.fotoPath)

                    OutlinedButton(onClick = alEscanearQr, modifier = Modifier.fillMaxWidth()) {
                        Text("Escanear código QR")
                    }
                    Text(
                        if (estado.codigoQr.isBlank()) "Sin código QR escaneado" else "QR: ${estado.codigoQr}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    estado.mensajeRecurso?.let {
                        Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (estado.tieneErrores) {
                Text("Revisa los campos marcados en rojo.", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = vm::guardar,
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.height(20.dp))
                } else {
                    Text(if (estado.esEdicion) "Guardar cambios" else "Registrar oportunidad")
                }
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        texto, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun FotoEvidencia(path: String?) {
    if (path == null) return
    val imagen = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() } ?: return
    Image(
        bitmap = imagen,
        contentDescription = "Foto de evidencia",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
    )
}
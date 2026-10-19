package cl.duoc.activaciones.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.activaciones.ActivacionesApp
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.data.local.OportunidadEntity
import cl.duoc.activaciones.util.aFecha

@Composable
fun app(): ActivacionesApp = LocalContext.current.applicationContext as ActivacionesApp

inline fun <VM : ViewModel> vmFactory(crossinline crear: () -> VM) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = crear() as T
}

@Composable
fun <T> Selector(
    etiqueta: String,
    opciones: List<T>,
    seleccion: T,
    texto: (T) -> String,
    modifier: Modifier = Modifier,
    onSeleccion: (T) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { abierto = true }) { Text("$etiqueta: ${texto(seleccion)}") }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(texto(opcion)) },
                    onClick = { onSeleccion(opcion); abierto = false }
                )
            }
        }
    }
}

/** Campo de texto con error visible (isError + supportingText) controlado desde el ViewModel. */
@Composable
fun Campo(
    etiqueta: String,
    valor: String,
    numerico: Boolean = false,
    unaLinea: Boolean = true,
    error: String? = null,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = if (numerico) KeyboardType.Number else KeyboardType.Text),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

/** Lista desplegable con aspecto de campo de formulario. Se puede deshabilitar (ej: comuna sin región). */
@Composable
fun <T> CampoSelector(
    etiqueta: String,
    opciones: List<T>,
    seleccion: T?,
    texto: (T) -> String,
    error: String? = null,
    habilitado: Boolean = true,
    onSeleccion: (T) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = seleccion?.let(texto) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else null,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        if (habilitado) Box(Modifier.matchParentSize().clickable { abierto = true })
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(texto(opcion)) },
                    onClick = { onSeleccion(opcion); abierto = false }
                )
            }
        }
    }
}

/** Diálogo de confirmación antes de eliminar cualquier registro. */
@Composable
fun ConfirmarEliminacion(
    titulo: String,
    mensaje: String,
    alCancelar: () -> Unit,
    alConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCancelar,
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = alConfirmar) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}

fun colorEstado(estado: EstadoOportunidad): Color = when (estado) {
    EstadoOportunidad.EVENTO_IDENTIFICADO -> Color(0xFF607D8B)
    EstadoOportunidad.CONTACTO_PENDIENTE -> Color(0xFFEF6C00)
    EstadoOportunidad.CONTACTO_REALIZADO -> Color(0xFF1976D2)
    EstadoOportunidad.PROPUESTA_ENVIADA -> Color(0xFF6A1B9A)
    EstadoOportunidad.EN_ESPERA_RESPUESTA -> Color(0xFF9E7700)
    EstadoOportunidad.PARTICIPACION_CONFIRMADA -> Color(0xFF2E7D32)
    EstadoOportunidad.DESCARTADA -> Color(0xFFC62828)
}

@Composable
fun EstadoChip(estado: EstadoOportunidad) {
    Surface(color = colorEstado(estado), shape = RoundedCornerShape(50)) {
        Text(
            estado.etiqueta, color = Color.White, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun OportunidadCard(
    o: OportunidadEntity,
    onEditar: (() -> Unit)? = null,
    onEliminar: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp).clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    o.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f)
                )
                if (onEditar != null) {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                if (onEliminar != null) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            val lugar = listOf(o.comuna, o.region).filter { it.isNotBlank() }.joinToString(", ")
            Text(
                o.tipo.etiqueta + if (lugar.isNotEmpty()) " · $lugar" else "",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EstadoChip(o.estado)
                Text(
                    "Prioridad ${o.prioridad.etiqueta}" + (o.fechaInicio?.let { " · ${it.aFecha()}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
package cl.duoc.activaciones.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.activaciones.model.AREAS_USUARIO
import cl.duoc.activaciones.model.ZONAS_USUARIO
import cl.duoc.activaciones.ui.components.Campo
import cl.duoc.activaciones.ui.components.CampoSelector
import cl.duoc.activaciones.ui.theme.barraColores
import cl.duoc.activaciones.viewmodel.UsuarioViewModel

@Composable
fun RegistroScreen(
    viewModel: UsuarioViewModel,
    alVolver: () -> Unit,
    alRegistrado: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val e = estado.errores
    val errorCorreo = e.correo

    // Al salir de la pantalla, el formulario queda limpio
    DisposableEffect(Unit) { onDispose { viewModel.limpiar() } }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = barraColores(),
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = alVolver) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Usa solo datos ficticios: esta es una demostración académica.",
                style = MaterialTheme.typography.bodySmall
            )

            Campo("Nombre completo *", estado.nombre, error = e.nombre, onCambio = viewModel::onNombreChange)

            OutlinedTextField(
                value = estado.correo,
                onValueChange = viewModel::onCorreoChange,
                label = { Text("Correo electrónico *") },
                singleLine = true,
                isError = errorCorreo != null,
                supportingText = if (errorCorreo != null) {
                    { Text(errorCorreo) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            CampoClave("Contraseña *", estado.clave, e.clave, viewModel::onClaveChange)
            CampoClave("Confirmar contraseña *", estado.confirmarClave, e.confirmarClave, viewModel::onConfirmarClaveChange)

            Campo("Dirección *", estado.direccion, error = e.direccion, onCambio = viewModel::onDireccionChange)

            CampoSelector(
                "Área *", AREAS_USUARIO, estado.area, { it },
                error = e.area, onSeleccion = viewModel::onAreaChange
            )
            CampoSelector(
                "Zona o sucursal *", ZONAS_USUARIO, estado.zona, { it },
                error = e.zona, onSeleccion = viewModel::onZonaChange
            )

            Text(
                "Perfil asignado: Ejecutivo comercial",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Checkbox(checked = estado.aceptaTerminos, onCheckedChange = viewModel::onAceptarTerminosChange)
                Text("Acepto los términos y condiciones")
            }
            e.terminos?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (e.hayErrores) {
                Text("Revisa los campos marcados en rojo.", color = MaterialTheme.colorScheme.error)
            }

            // Si el formulario es válido, registra y navega a "resumen"
            Button(
                onClick = { viewModel.registrar(alRegistrado) },
                enabled = !estado.registrando,
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 4.dp)
            ) {
                if (estado.registrando) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(20.dp)
                    )
                } else {
                    Text("Registrar")
                }
            }
        }
    }
}

@Composable
private fun CampoClave(etiqueta: String, valor: String, error: String?, onCambio: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = { TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") } },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}
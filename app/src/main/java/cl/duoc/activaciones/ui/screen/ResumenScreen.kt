package cl.duoc.activaciones.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.activaciones.util.aFechaHora
import cl.duoc.activaciones.viewmodel.UsuarioViewModel

@Composable
fun ResumenScreen(viewModel: UsuarioViewModel, alIrALogin: () -> Unit) {
    val usuario by viewModel.ultimoRegistro.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("¡Cuenta creada!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        usuario?.let { u ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nombre: ${u.nombre}")
                    Text("Correo: ${u.correo}")
                    Text("Dirección: ${u.direccion}")
                    Text("Perfil: ${u.perfil.name.lowercase().replaceFirstChar { it.uppercase() }}")
                    Text("Área: ${u.area}")
                    Text("Zona: ${u.zona}")
                    Text("Estado de la cuenta: ${u.estadoCuenta.etiqueta}")
                    Text("Creada: ${u.fechaCreacion.aFechaHora()}")
                }
            }
        }
        Button(onClick = alIrALogin, modifier = Modifier.fillMaxWidth()) { Text("Ir a iniciar sesión") }
    }
}
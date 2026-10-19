package cl.duoc.activaciones.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.activaciones.ui.components.app
import cl.duoc.activaciones.ui.components.vmFactory
import cl.duoc.activaciones.ui.theme.barraColores
import cl.duoc.activaciones.util.AlertasWorker
import cl.duoc.activaciones.viewmodel.UsuarioViewModel

@Composable
fun PerfilScreen(alVolver: () -> Unit, alSalir: () -> Unit) {
    val app = app()
    val vm: UsuarioViewModel = viewModel(
        factory = vmFactory { UsuarioViewModel(app.repo, app.oportunidadRepo, app.sesion) }
    )
    val usuario by vm.usuario.collectAsState()

    // El permiso se pide aquí, cuando el usuario lo necesita (no al abrir la app).
    // Si lo deniega, el worker simplemente no muestra la notificación.
    val permisoNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        AlertasWorker.ejecutarAhora(app)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = barraColores(),
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            usuario?.let { u ->
                Text(u.nombre, style = MaterialTheme.typography.headlineSmall)
                Text("Correo: ${u.correo}")
                Text("Perfil: ${u.perfil.name.lowercase().replaceFirstChar { it.uppercase() }}")
                Text("Zona: ${u.zona}")
            }
            OutlinedButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        permisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        AlertasWorker.ejecutarAhora(app)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Probar notificación de alertas") }
            Button(
                onClick = { vm.cerrarSesion(); alSalir() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar sesión") }
        }
    }
}
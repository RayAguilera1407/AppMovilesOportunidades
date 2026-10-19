package cl.duoc.activaciones.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.activaciones.data.local.OportunidadEntity

import cl.duoc.activaciones.viewmodel.UsuarioViewModel
import cl.duoc.activaciones.ui.components.OportunidadCard
import cl.duoc.activaciones.ui.theme.barraColores
import cl.duoc.activaciones.ui.components.app
import cl.duoc.activaciones.ui.components.vmFactory

@Composable
fun AlertasScreen(alVolver: () -> Unit, alAbrir: (Long) -> Unit) {
    val app = app()
    val vm: UsuarioViewModel = viewModel(factory = vmFactory { UsuarioViewModel(app.repo, app.oportunidadRepo, app.sesion) })
    val alertas by vm.alertas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = barraColores(),
                title = { Text("Alertas (${alertas.total})") },
                navigationIcon = { IconButton(onClick = alVolver) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize()) {
            seccion("Seguimientos atrasados", alertas.seguimientosAtrasados, alAbrir)
            seccion("Eventos próximos (14 días)", alertas.eventosProximos, alAbrir)
            seccion("Oportunidades sin contacto", alertas.sinContacto, alAbrir)
            seccion("Propuestas sin respuesta (+7 días)", alertas.propuestasSinRespuesta, alAbrir)
        }
    }
}

private fun LazyListScope.seccion(titulo: String, lista: List<OportunidadEntity>, alAbrir: (Long) -> Unit) {
    item(key = titulo) {
        Text(
            "$titulo · ${lista.size}", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )
    }
    if (lista.isEmpty()) {
        item(key = "$titulo-vacio") { Text("Sin pendientes.", Modifier.padding(horizontal = 16.dp)) }
    }
    items(lista, key = { "$titulo-${it.id}" }) { o -> OportunidadCard(o) { alAbrir(o.id) } }
}

package cl.duoc.activaciones.ui.oportunidad

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.model.Filtro
import cl.duoc.activaciones.data.local.OportunidadEntity
import cl.duoc.activaciones.model.Perfil
import cl.duoc.activaciones.model.Prioridad
import cl.duoc.activaciones.model.REGIONES
import cl.duoc.activaciones.model.TipoEvento
import cl.duoc.activaciones.ui.components.ConfirmarEliminacion
import cl.duoc.activaciones.ui.components.OportunidadCard
import cl.duoc.activaciones.ui.components.Selector
import cl.duoc.activaciones.ui.components.app
import cl.duoc.activaciones.ui.components.vmFactory
import cl.duoc.activaciones.ui.theme.barraColores

@Composable
fun ListaOportunidadesScreen(
    alVolver: () -> Unit,
    alAbrir: (Long) -> Unit,
    alEditar: (Long) -> Unit,
    alCrear: () -> Unit
) {
    val app = app()
    val vm: OportunidadViewModel = viewModel(
        factory = vmFactory { OportunidadViewModel(app.oportunidadRepo, app.repo, app.sesion) }
    )
    val lista by vm.lista.collectAsState()
    val filtro by vm.filtro.collectAsState()
    val usuario by vm.usuario.collectAsState()
    val usuarios by vm.usuarios.collectAsState()
    var porEliminar by remember { mutableStateOf<OportunidadEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = barraColores(),
                title = { Text("Oportunidades (${lista.size})") },
                navigationIcon = {
                    IconButton(onClick = alVolver) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = alCrear,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) { Icon(Icons.Default.Add, contentDescription = "Nueva oportunidad") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(
                value = filtro.texto,
                onValueChange = { vm.actualizarFiltro(filtro.copy(texto = it)) },
                label = { Text("Buscar por nombre, descripción o comuna") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Selector("Estado", listOf<EstadoOportunidad?>(null) + EstadoOportunidad.values(), filtro.estado, { it?.etiqueta ?: "Todos" }) {
                    vm.actualizarFiltro(filtro.copy(estado = it))
                }
                Selector("Prioridad", listOf<Prioridad?>(null) + Prioridad.values(), filtro.prioridad, { it?.etiqueta ?: "Todas" }) {
                    vm.actualizarFiltro(filtro.copy(prioridad = it))
                }
                Selector("Tipo", listOf<TipoEvento?>(null) + TipoEvento.values(), filtro.tipo, { it?.etiqueta ?: "Todos" }) {
                    vm.actualizarFiltro(filtro.copy(tipo = it))
                }
                Selector("Región", listOf<String?>(null) + REGIONES, filtro.region, { it ?: "Todas" }) {
                    vm.actualizarFiltro(filtro.copy(region = it))
                }
                if (usuario?.perfil == Perfil.SUPERVISOR) {
                    val ejecutivos = usuarios.filter { it.perfil == Perfil.EJECUTIVO }
                    Selector(
                        "Responsable", listOf<Long?>(null) + ejecutivos.map { it.id }, filtro.responsableId,
                        { id -> ejecutivos.firstOrNull { it.id == id }?.nombre ?: "Todos" }
                    ) { vm.actualizarFiltro(filtro.copy(responsableId = it)) }
                }
                TextButton(onClick = { vm.actualizarFiltro(Filtro()) }) { Text("Limpiar") }
            }
            if (lista.isEmpty()) {
                Text("No hay oportunidades registradas para estos filtros.", Modifier.padding(16.dp))
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(lista, key = { it.id }) { o ->
                    OportunidadCard(o, onEditar = { alEditar(o.id) }, onEliminar = { porEliminar = o }) { alAbrir(o.id) }
                }
            }
        }
    }

    porEliminar?.let { o ->
        ConfirmarEliminacion(
            titulo = "Eliminar oportunidad",
            mensaje = "Se eliminará \"${o.nombre}\" junto con su historial y evidencias. Esta acción no se puede deshacer.",
            alCancelar = { porEliminar = null },
            alConfirmar = {
                vm.eliminar(o)
                porEliminar = null
            }
        )
    }
}
package cl.duoc.activaciones.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.activaciones.viewmodel.PrincipalViewModel
import cl.duoc.activaciones.ui.components.app
import cl.duoc.activaciones.ui.components.vmFactory
import cl.duoc.activaciones.ui.theme.DegradadoRosaMorado

@Composable
fun MenuScreen(
    alOportunidades: () -> Unit,
    alAlertas: () -> Unit,
    alOrganizaciones: () -> Unit,
    alPerfil: () -> Unit
) {
    val app = app()
    val vm: PrincipalViewModel = viewModel(
        factory = vmFactory { PrincipalViewModel(app.repo, app.oportunidadRepo, app.sesion) }
    )
    val usuario by vm.usuario.collectAsState()
    val alertas by vm.alertas.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Column(
            Modifier.fillMaxWidth()
                .background(DegradadoRosaMorado, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .padding(horizontal = 24.dp, vertical = 36.dp)
        ) {
            Text("Hola, ${usuario?.nombre?.substringBefore(' ') ?: ""}", color = Color.White,
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Gestión de oportunidades y activaciones", color = Color.White.copy(alpha = 0.9f))
            usuario?.let {
                Text("${it.perfil.name.lowercase().replaceFirstChar { c -> c.uppercase() }} · ${it.zona}",
                    color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OpcionMenu("Oportunidades", "Registrar, consultar y editar", Icons.Default.List, MaterialTheme.colorScheme.primary, alOportunidades)
            OpcionMenu("Alertas", "${alertas.total} pendientes", Icons.Default.Notifications, MaterialTheme.colorScheme.secondary, alAlertas)
            OpcionMenu("Organizaciones", "Empresas, instituciones y contactos", Icons.Default.Home, MaterialTheme.colorScheme.primary, alOrganizaciones)
            OpcionMenu("Mi perfil", "Datos de la cuenta y sesión", Icons.Default.Person, MaterialTheme.colorScheme.secondary, alPerfil)
        }
    }
}

@Composable
private fun OpcionMenu(titulo: String, subtitulo: String, icono: ImageVector, color: Color, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icono, contentDescription = null, tint = Color.White,
                modifier = Modifier.background(color, RoundedCornerShape(12.dp)).padding(10.dp).size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
